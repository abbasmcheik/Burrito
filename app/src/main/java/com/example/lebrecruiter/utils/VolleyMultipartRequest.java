package com.example.lebrecruiter.utils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class VolleyMultipartRequest extends Request<NetworkResponse> {
    //This class only purpose is to handle multi-part file transfer

    private final Response.Listener<NetworkResponse> mListener;
    private final Map<String, String> mHeaders;

    public VolleyMultipartRequest(int method, String url, Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mHeaders = new HashMap<>();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + getBoundary();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Add file parts
            Map<String, DataPart> fileParams = getByteData();
            if (fileParams != null) {
                for (Map.Entry<String, DataPart> entry : fileParams.entrySet()) {
                    addFilePart(entry.getKey(), entry.getValue(), bos);
                }
            }
            bos.write(("--" + getBoundary() + "--\r\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, null);
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    protected abstract Map<String, DataPart> getByteData() throws AuthFailureError;

    private void addFilePart(String fieldName, DataPart dataPart, ByteArrayOutputStream bos) throws IOException {
        String header = "--" + getBoundary() + "\r\n"
                + "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\""
                + dataPart.getFileName() + "\"\r\n"
                + "Content-Type: " + dataPart.getMimeType() + "\r\n\r\n";
        bos.write(header.getBytes());
        bos.write(dataPart.getContent());
        bos.write("\r\n".getBytes());
    }

    private String getBoundary() {
        return "apiclient-" + System.currentTimeMillis();
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String mimeType;

        public DataPart(String fileName, byte[] content, String mimeType) {
            this.fileName = fileName;
            this.content = content;
            this.mimeType = mimeType;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getMimeType() {
            return mimeType;
        }
    }
}
