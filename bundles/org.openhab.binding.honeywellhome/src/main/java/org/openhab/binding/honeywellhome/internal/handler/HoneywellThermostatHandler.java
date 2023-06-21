/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.honeywellhome.internal.handler;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.honeywellhome.client.HoneywellClient;
import org.openhab.binding.honeywellhome.client.api.response.GetThermostatsStatusResponse;
import org.openhab.binding.honeywellhome.internal.HoneywellHomeThermostatConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.honeywellhome.internal.HoneywellHomeBindingConstants.*;

/**
 * The {@link HoneywellThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Idan - Initial contribution
 */

public class HoneywellThermostatHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HoneywellThermostatHandler.class);

    private @Nullable HoneywellHomeThermostatConfiguration config;

    protected @Nullable ScheduledFuture<?> refreshTask;

    public HoneywellThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (COOL_SET_POINT.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                startRefreshTask();
            }

            if(command.toString().equals("state")) {
                getHoneywellClient().changeThermostatsSetting("", "", "", 0, 0, "");
            }

        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(HoneywellHomeThermostatConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        startRefreshTask();

    }

    private void startRefreshTask() {
        disposeRefreshTask();
        int currentRefreshInterval = 10; //todo add validation and pull from config
        refreshTask = scheduler.scheduleWithFixedDelay(this::update, 0, currentRefreshInterval, TimeUnit.SECONDS); //todo make sure it's keep running in case of any failure
    }

    private void disposeRefreshTask() {
        ScheduledFuture<?> localRefreshTask = refreshTask;
        if (localRefreshTask != null) {
            localRefreshTask.cancel(true);
            this.refreshTask = null;
        }
    }

    private void update() {
        try {
            String locationId = config.locationId;
            String deviceId = config.deviceId;
            if(getHoneywellClient() != null) {
                GetThermostatsStatusResponse getThermostatsStatusResponse = getHoneywellClient().getThermostatsDevice(deviceId, locationId);
                updateState(COOL_SET_POINT, new DecimalType(getThermostatsStatusResponse.changeableValues.coolSetpoint));
                updateState(HEAT_SET_POINT, new DecimalType(getThermostatsStatusResponse.changeableValues.heatSetpoint));
                updateState(THERMOSTAT_SET_POINT_STATUS, new StringType(getThermostatsStatusResponse.changeableValues.thermostatSetpointStatus));
                updateState(HEAT_COOL_MODE, new StringType(getThermostatsStatusResponse.changeableValues.heatCoolMode));
                updateState(MODE, new StringType(getThermostatsStatusResponse.changeableValues.mode));
            }
        } catch (Exception e) {
            logger.error("Got error on Thermostat Handler update method", e);
        }

    }

    private HoneywellClient getHoneywellClient() {
        HoneywellHomeHandler honeywellHomeHandler = (HoneywellHomeHandler)getBridge().getHandler(); // todo make it safe
        return honeywellHomeHandler.getHoneywellClient();
    }
}
