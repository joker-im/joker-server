package im.joker.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Device implements IDevice {

    private String deviceId;

    private String accessToken;

    private String username;

    private String name;

    private String userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Device device = (Device) o;
        return Objects.equals(deviceId, device.deviceId) &&
                Objects.equals(username, device.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, username);
    }

    @Override
    public String getUserId() {
        return userId;
    }
}
