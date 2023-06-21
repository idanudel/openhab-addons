package org.openhab.binding.honeywellhome.client.api.request;

public class ChangeThermostatsSettingRequest {

    String mode;
    int heatSetpoint;
    int coolSetpoint;
    String thermostatSetpointStatus;

    public ChangeThermostatsSettingRequest(String mode, int heatSetpoint, int coolSetpoint, String thermostatSetpointStatus) {
        this.mode = mode;
        this.heatSetpoint = heatSetpoint;
        this.coolSetpoint = coolSetpoint;
        this.thermostatSetpointStatus = thermostatSetpointStatus;
    }
}
