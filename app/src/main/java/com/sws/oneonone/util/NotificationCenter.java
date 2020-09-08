package com.sws.oneonone.util;

import android.util.SparseArray;

import java.util.ArrayList;

public class NotificationCenter {
    private static int totalEvents = 1;
    public static final int commentData = totalEvents++;
    public static final int selectedItem = totalEvents++;
    public static final int trimVideo = totalEvents++;;
    public static final int trimAcceptedVideo = totalEvents++;
    public static final int viewChallenge = totalEvents++;
    public static final int alertDialog = totalEvents++;
    public static final int isFollow = totalEvents++;

    private SparseArray<ArrayList<Object>> observers = new SparseArray<>();
    private SparseArray<ArrayList<Object>> removeAfterBroadcast = new SparseArray<>();
    private SparseArray<ArrayList<Object>> addAfterBroadcast = new SparseArray<>();
    private ArrayList<DelayedPost> delayedPosts = new ArrayList<>(10);

    private int broadcasting = 0;
    private boolean animationInProgress;

    private int[] allowedNotifications;

    public interface NotificationCenterDelegate {
        void didReceivedNotification(int id, Object... args);
    }

    private class DelayedPost {

        private DelayedPost(int id, Object[] args) {
            this.id = id;
            this.args = args;
        }

        private int id;
        private Object[] args;
    }

    private static volatile NotificationCenter Instance = null;

    public static NotificationCenter getInstance() {
        NotificationCenter localInstance = Instance;
        if (localInstance == null) {
            synchronized (NotificationCenter.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new NotificationCenter();
                }
            }
        }
        return localInstance;
    }

    public void setAllowedNotificationsDuringAnimation(int notifications[]) {
        allowedNotifications = notifications;
    }

    public void setAnimationInProgress(boolean value) {
        animationInProgress = value;
        if (!animationInProgress && !delayedPosts.isEmpty()) {
            for (DelayedPost delayedPost : delayedPosts) {
                postNotificationNameInternal(delayedPost.id, true, delayedPost.args);
            }
            delayedPosts.clear();
        }
    }

    public boolean isAnimationInProgress() {
        return animationInProgress;
    }

    public void postNotificationName(int id, Object... args) {
        boolean allowDuringAnimation = false;
        if (allowedNotifications != null) {
            for (int a = 0; a < allowedNotifications.length; a++) {
                if (allowedNotifications[a] == id) {
                    allowDuringAnimation = true;
                    break;
                }
            }
        }
        postNotificationNameInternal(id, allowDuringAnimation, args);
    }

    public void postNotificationNameInternal(int id, boolean allowDuringAnimation, Object... args) {
        if (BuildVars.DEBUG_VERSION) {
            if (Thread.currentThread() != ApplicationLoader.applicationHandler.getLooper().getThread()) {
                throw new RuntimeException("postNotificationName allowed only from MAIN thread");
            }
        }
        if (!allowDuringAnimation && animationInProgress) {
            DelayedPost delayedPost = new DelayedPost(id, args);
            delayedPosts.add(delayedPost);
            if (BuildVars.DEBUG_VERSION) {

            }
            return;
        }
        broadcasting++;
        ArrayList<Object> objects = observers.get(id);
        if (objects != null && !objects.isEmpty()) {
            for (int a = 0; a < objects.size(); a++) {
                Object obj = objects.get(a);
                ((NotificationCenterDelegate) obj).didReceivedNotification(id, args);
            }
        }
        broadcasting--;
        if (broadcasting == 0) {
            if (removeAfterBroadcast.size() != 0) {
                for (int a = 0; a < removeAfterBroadcast.size(); a++) {
                    int key = removeAfterBroadcast.keyAt(a);
                    ArrayList<Object> arrayList = removeAfterBroadcast.get(key);
                    for (int b = 0; b < arrayList.size(); b++) {
                        removeObserver(arrayList.get(b), key);
                    }
                }
                removeAfterBroadcast.clear();
            }
            if (addAfterBroadcast.size() != 0) {
                for (int a = 0; a < addAfterBroadcast.size(); a++) {
                    int key = addAfterBroadcast.keyAt(a);
                    ArrayList<Object> arrayList = addAfterBroadcast.get(key);
                    for (int b = 0; b < arrayList.size(); b++) {
                        addObserver(arrayList.get(b), key);
                    }
                }
                addAfterBroadcast.clear();
            }
        }
    }

    public void addObserver(Object observer, int id) {
        if (BuildVars.DEBUG_VERSION) {
            if (Thread.currentThread() != ApplicationLoader.applicationHandler.getLooper().getThread()) {
                throw new RuntimeException("addObserver allowed only from MAIN thread");
            }
        }
        if (broadcasting != 0) {
            ArrayList<Object> arrayList = addAfterBroadcast.get(id);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                addAfterBroadcast.put(id, arrayList);
            }
            arrayList.add(observer);
            return;
        }
        ArrayList<Object> objects = observers.get(id);
        if (objects == null) {
            observers.put(id, (objects = new ArrayList<>()));
        }
        if (objects.contains(observer)) {
            return;
        }
        objects.add(observer);
    }

    public void removeObserver(Object observer, int id) {
        if (BuildVars.DEBUG_VERSION) {
            if (Thread.currentThread() != ApplicationLoader.applicationHandler.getLooper().getThread()) {
                throw new RuntimeException("removeObserver allowed only from MAIN thread");
            }
        }
        if (broadcasting != 0) {
            ArrayList<Object> arrayList = removeAfterBroadcast.get(id);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                removeAfterBroadcast.put(id, arrayList);
            }
            arrayList.add(observer);
            return;
        }
        ArrayList<Object> objects = observers.get(id);
        if (objects != null) {
            objects.remove(observer);
        }
    }
}