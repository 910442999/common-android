package com.yuanquan.common.api.response;

import com.yuanquan.common.interfaces.ProgressListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

public class ProgressRequestBody extends RequestBody {

    private final RequestBody requestBody;
    private final ProgressListener progressListener;
    private long currentBytesWritten;
    private long totalContentLength;
    private int lastReportedProgress = -1;

    public ProgressRequestBody(RequestBody requestBody, ProgressListener listener) {
        this.requestBody = requestBody;
        this.progressListener = listener;
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        totalContentLength = contentLength();
        BufferedSink bufferedSink = Okio.buffer(new ForwardingSink(sink) {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                currentBytesWritten += byteCount;
                notifyProgress();
            }

            private void notifyProgress() {
                if (progressListener == null) return;

                if (totalContentLength > 0) {
                    // 计算进度百分比，使用浮点避免整数溢出
                    int progress = (int) ((currentBytesWritten * 100.0) / totalContentLength);
                    if (progress != lastReportedProgress) {
                        lastReportedProgress = progress;
                        progressListener.onProgress(progress);
                    }
                } else {
                    // 总长度未知时回调特殊值-1
                    progressListener.onProgress(-1);
                }
            }
        });

        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }
}