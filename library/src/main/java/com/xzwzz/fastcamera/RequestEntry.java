package com.xzwzz.fastcamera;


import com.xzwzz.fastcamera.callback.CameraCallback;

/**
 * @author xzwzz
 * @time 2019-11-19
 * @package com.fc62.raisebook.videolive.image.entity
 */
class RequestEntry {
    private CameraCallback callback;
    private Runnable runnable;
    private int requestCode;

    private RequestEntry() {
    }

    public RequestEntry newInstance(Builder builder) {
        this.callback = builder.callback;
        this.runnable = builder.runnable;
        this.requestCode = builder.requestCode;
        return this;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public CameraCallback getCallback() {
        return callback;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public static class Builder {
        private CameraCallback callback;
        private Runnable runnable;
        private int requestCode;

        public Builder withRequestCode(int code) {
            this.requestCode = code;
            return this;
        }

        public Builder withCallback(CameraCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder withRunnable(Runnable runnable) {
            this.runnable = runnable;
            return this;
        }

        public RequestEntry build() {
            RequestEntry entry = new RequestEntry();
            return entry.newInstance(this);
        }
    }
}
