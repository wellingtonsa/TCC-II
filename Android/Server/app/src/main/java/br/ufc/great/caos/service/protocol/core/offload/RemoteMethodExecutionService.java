/*******************************************************************************
 * Copyright (c) 2015 LG Electronics. All Rights Reserved. This software is the
 * confidential and proprietary information of LG Electronics. You shall not
 * disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with LG Electronics.
 *******************************************************************************/
package br.ufc.great.caos.service.protocol.core.offload;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import br.ufc.great.caos.service.protocol.server.util.injection.MetadataLoader;

public class RemoteMethodExecutionService extends Service {

    private Context context;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public RemoteMethodExecutionService(Context context){
        this.context = context;
    }


    public void replyToCloud(Object resultFromOffload, Socket clientSocket) throws IOException {
        OutputStream out = clientSocket.getOutputStream();

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);

        objectOutputStream.writeObject(resultFromOffload);
        objectOutputStream.close();
        clientSocket.close();
    }

    public Object executeMethod(InvocableMethod invocableMethod) {

        try {
            MetadataLoader metadataLoader = new MetadataLoader(context);
            metadataLoader.loadMetadata(invocableMethod.getAppName());

            final Class<Object> classToLoad = (Class<Object>) metadataLoader.getClassLoader().loadClass(invocableMethod.getPackageName()+"."+invocableMethod.getClassName());

            final Object instance  = classToLoad.newInstance();

            Method method = getMethod(classToLoad, invocableMethod.getMethodName());

            return method.invoke(instance, invocableMethod.getParam());
        } catch (InstantiationException e) {
            Log.e("executeMethod", "InstantiationException", e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e("executeMethod", "IllegalAccessException", e);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e("executeMethod", "ClassNotFoundException", e);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Method getMethod (Class<Object> classToLoad, String methodName){
        for(Method m : classToLoad.getMethods()){
            if(m.getName().equals(methodName)){
                return m;
            }
        }
        return null;
    }
}



