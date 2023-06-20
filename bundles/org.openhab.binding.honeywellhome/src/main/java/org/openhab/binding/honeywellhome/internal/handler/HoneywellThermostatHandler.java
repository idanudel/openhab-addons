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
import org.openhab.binding.honeywellhome.client.api.response.GetThermostatsStatusResponse;
import org.openhab.binding.honeywellhome.internal.HoneywellHomeThermostatConfiguration;
import org.openhab.core.library.types.DecimalType;
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

import static org.openhab.binding.honeywellhome.internal.HoneywellHomeBindingConstants.COOL_SET_POINT;

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
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
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
        int currentRefreshInterval = 10; //todo add validation
        refreshTask = scheduler.scheduleWithFixedDelay(this::update, 0, currentRefreshInterval, TimeUnit.SECONDS);
    }

    private void disposeRefreshTask() {
        ScheduledFuture<?> localRefreshTask = refreshTask;
        if (localRefreshTask != null) {
            localRefreshTask.cancel(true);
            this.refreshTask = null;
        }
    }

    private void update() {
        String locationId = config.locationId;
        String deviceId = config.deviceId;
        HoneywellHomeHandler honeywellHomeHandler = (HoneywellHomeHandler)getBridge().getHandler(); // todo make it safe
        GetThermostatsStatusResponse getThermostatsStatusResponse = honeywellHomeHandler.getHoneywellClient().getThermostatsDevice(deviceId, locationId);
        updateState(COOL_SET_POINT, new DecimalType(getThermostatsStatusResponse.changeableValues.coolSetpoint));
    }
}
