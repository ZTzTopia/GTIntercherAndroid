package com.anzu.sdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;

import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public final class AdvertisingIdClientInfo {
    public static final class AdInfo {
        private final String advertisingId;
        private final boolean limitAdTrackingEnabled;

        AdInfo(String advertisingId, boolean limitAdTrackingEnabled) {
            this.advertisingId = advertisingId;
            this.limitAdTrackingEnabled = limitAdTrackingEnabled;
        }

        public String getId() {
            return advertisingId;
        }

        public boolean isLimitAdTrackingEnabled() {
            return limitAdTrackingEnabled;
        }
    }

    private static final class AdvertisingConnection implements ServiceConnection {
        private final LinkedBlockingQueue<IBinder> queue;
        boolean retrieved;

        private AdvertisingConnection() {
            retrieved = false;
            queue = new LinkedBlockingQueue<>(1);
        }

        public IBinder getBinder() throws InterruptedException {
            if (!retrieved) {
                retrieved = true;
                return queue.take();
            }
            throw new IllegalStateException();
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                this.queue.put(iBinder);
            } catch (InterruptedException e) {
                /* ~ */
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            /* ~ */
        }
    }

    private static final class AdvertisingInterface implements IInterface {
        private final IBinder binder;

        public AdvertisingInterface(IBinder binder) {
            this.binder = binder;
        }

        public IBinder asBinder() {
            return binder;
        }

        public String getId() throws RemoteException {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();

            try {
                obtain.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                binder.transact(1, obtain, obtain2, 0);
                obtain2.readException();
                return obtain2.readString();
            } finally {
                obtain2.recycle();
                obtain.recycle();
            }
        }

        public boolean isLimitAdTrackingEnabled(boolean z) throws RemoteException {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();

            try {
                obtain.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                obtain.writeInt(z ? 1 : 0);
                binder.transact(2, obtain, obtain2, 0);
                obtain2.readException();
                return obtain2.readInt() != 0;
            } finally {
                obtain2.recycle();
                obtain.recycle();
            }
        }
    }

    public static AdInfo getAdvertisingIdInfo(Context context) throws Exception {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            context.getPackageManager().getPackageInfo(GooglePlayServicesUtil.GOOGLE_PLAY_STORE_PACKAGE, 0);
            AdvertisingConnection advertisingConnection = new AdvertisingConnection();
            Intent intent = new Intent("com.google.android.gms.ads.identifier.service.START");
            intent.setPackage("com.google.android.gms");
            if (context.bindService(intent, advertisingConnection, Context.BIND_AUTO_CREATE)) {
                try {
                    AdvertisingInterface advertisingInterface = new AdvertisingInterface(advertisingConnection.getBinder());
                    AdInfo adInfo = new AdInfo(advertisingInterface.getId(), advertisingInterface.isLimitAdTrackingEnabled(true));
                    context.unbindService(advertisingConnection);
                    return adInfo;
                } catch (Exception e) {
                    throw e;
                } catch (Throwable th) {
                    context.unbindService(advertisingConnection);
                    throw th;
                }
            } else {
                throw new IOException("Google Play connection failed");
            }
        } else {
            throw new IllegalStateException("Cannot be called from the main thread");
        }
    }
}
