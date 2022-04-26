/*******************************************************************************
 * Copyright (c) 2015 LG Electronics. All Rights Reserved. This software is the
 * confidential and proprietary information of LG Electronics. You shall not
 * disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with LG Electronics.
 *******************************************************************************/
package com.example.apkloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import br.ufc.great.caos.api.offload.InvocableMethod;
import br.ufc.great.caos.api.offload.OffloadingError;

public class RemoteMethodExecutionService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

    	new Thread(new RemoteMethodExecutionThreadPool("RemoteMethodExecutionService",
    			NetworkInfo.getLocalIP(), 9000)).start();
    	
        return super.onStartCommand(intent, flags, startId);
    }
    
    private class RemoteMethodExecutionThreadPool extends ServiceThreadPool{

    	public RemoteMethodExecutionThreadPool(String name, String ip, int port) {
    		super(name, ip, port);
    		// TODO Auto-generated constructor stub
    	}

    	@Override
    	public void handleClientRequest(Socket clientSocket) throws IOException {
    		
    		InputStream in = null;
            ObjectInputStream receivedStream = null;
    		
    		in = clientSocket.getInputStream();

            receivedStream = new ObjectInputStream(in);
            
            OffloadingError offloadingErrors = new OffloadingError();

            try {
            	Object object = receivedStream.readObject();

                if (object instanceof InvocableMethod) {
                    Log.i("GET CLASS NAME", ((InvocableMethod) object)
                            .getClassName());
                    Log.i("GET METHOD NAME", ((InvocableMethod) object)
                            .getMethodName());

                    InvocableMethod invocableMethod = (InvocableMethod) object;
                    
                    Object resultFromOffload = executeMethod(invocableMethod);

                    replyToCloud(resultFromOffload, clientSocket);
                    
                }
            }  catch (ClassNotFoundException e) {

                offloadingErrors.add(e.getMessage());
                replyToCloud(offloadingErrors, clientSocket);

                e.printStackTrace();
            } finally {
                in.close();
                receivedStream.close();
                clientSocket.close();
            }
    	}
    }

    private void replyToCloud(Object resultFromOffload, Socket clientSocket) throws IOException {
        OutputStream out = clientSocket.getOutputStream();

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);

        objectOutputStream.writeObject(resultFromOffload);
        objectOutputStream.close();
        clientSocket.close();
    }

    private Object executeMethod(InvocableMethod invocableMethod) {
        OffloadingError offloadingErrors = new OffloadingError();

        try {
            String apkName =
                    invocableMethod.getPackageName()+ "_" + invocableMethod.getAppVersion() + ".apk";
            File appDirectory = loadAPKFile(apkName);

            APKLoader apkLoader =
                    (APKLoader) APKLoader.newInstance(appDirectory,
                            getCacheDir().getAbsolutePath(), getClassLoader());

            Class< ? > classToLoad = apkLoader.loadClass(invocableMethod.getClassName());

            ClassLoaderWrapper classLoader = new ClassLoaderWrapper(classToLoad);

            return classLoader.executeMethod(classToLoad.newInstance(), invocableMethod);

        } catch (InstantiationException e) {
            offloadingErrors.add(e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            offloadingErrors.add(e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            offloadingErrors.add(e.getMessage());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            offloadingErrors.add(e.getMessage());
            e.printStackTrace();
        }

        return offloadingErrors;
    }

    private File loadAPKFile(String appName) throws FileNotFoundException {
        String pathExternalStorage = Environment.getExternalStorageDirectory().toString();
        File apkFile = new File(pathExternalStorage + "/caos-apks/" + appName);

        if (apkFile.exists()) {
            return apkFile;
        }
        throw new FileNotFoundException("APK FILE does not exist");

    }
}
