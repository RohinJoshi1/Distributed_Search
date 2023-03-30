

package networking;

import com.google.protobuf.InvalidProtocolBufferException;

public interface OnRequestCallback {
    byte[] handleRequest(byte[] requestPayload);

    String getEndpoint();
}
