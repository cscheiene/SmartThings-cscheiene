/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Based on Smartthings Fibaro Smoke Sensor DTH, removed unused stuff and added some minor things
 *  Not in anyway responsible for any missed Smoke alarm notifications. Tested ok with real smoke on deivce, but use at your own risk
 *
 *
 *
 */
 
metadata {
    definition (name: "Dlink z310 Smoke", namespace: "cscheiene", author: "SmartThings,cscheiene", mnmn: "SmartThings", vid: "generic-smoke-co", ocfDeviceType: "x.com.st.d.sensor.smoke") {
        capability "Battery" //attributes: battery
        capability "Sensor"
        capability "Smoke Detector" //attributes: smoke ("detected","clear","tested")
        capability "Health Check"
        attribute "tamper", "enum", ["detected", "clear"]
        fingerprint mfr:"0108", prod:"0002", model:"001E"
    }

    tiles (scale: 2){
        multiAttributeTile(name:"smoke", type: "lighting", width: 6, height: 4){
            tileAttribute ("device.smoke", key: "PRIMARY_CONTROL") {
                attributeState("clear", label:"CLEAR", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
                attributeState("detected", label:"SMOKE", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
                attributeState("tested", label:"TEST", icon:"st.alarm.smoke.test", backgroundColor:"#e86d13")
                attributeState("replacement required", label:"REPLACE", icon:"st.alarm.smoke.test", backgroundColor:"#FFFF66")
                attributeState("unknown", label:"UNKNOWN", icon:"st.alarm.smoke.test", backgroundColor:"#ffffff")
            }
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
                attributeState "battery", label:'Battery: ${currentValue}%', unit:"%"
            }
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "battery", label:'${currentValue}% battery', unit:"%"
        }

        main "smoke"
        details(["smoke"])
    }
}

def installed() {
	sendEvent(name: "checkInterval", value: 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def updated() {
	sendEvent(name: "checkInterval", value: 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}


def parse(String description) {
    log.debug "parse() >> description: $description"
    def result = null
    if (description.startsWith("Err 106")) {
        log.debug "parse() >> Err 106"
        result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
                descriptionText: "This sensor failed to complete the network security key exchange. " +
                        "If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
    } else if (description != "updated") {
        log.debug "parse() >> zwave.parse(description)"
        def cmd = zwave.parse(description, [0x31: 5, 0x71: 3, 0x84: 1])
        if (cmd) {
            result = zwaveEvent(cmd)
        }
    }
    log.debug "After zwaveEvent(cmd) >> Parsed '${description}' to ${result.inspect()}"
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    log.info "Executing zwaveEvent 86 (VersionV1): 12 (VersionReport) with cmd: $cmd"
    def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
    updateDataValue("fw", fw)
    def text = "$device.displayName: firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
    createEvent(descriptionText: text, isStateChange: false)
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF) {
        map.value = 1
        map.descriptionText = "${device.displayName} battery is low"
        map.isStateChange = true
    } else {
        map.value = cmd.batteryLevel
    }
    state.lastbatt = now()
    createEvent(map)
}


def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    setSecured()
    def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 5, 0x71: 3, 0x84: 1])
    if (encapsulatedCommand) {
        //log.debug "command: 98 (Security) 81(SecurityMessageEncapsulation) encapsulatedCommand:  $encapsulatedCommand"
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract encapsulated cmd from $cmd"
        createEvent(descriptionText: cmd.toString())
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
    log.info "Executing zwaveEvent 98 (SecurityV1): 03 (SecurityCommandsSupportedReport) with cmd: $cmd"
    setSecured()
    log.info "checking this MSR : ${getDataValue("MSR")} before sending configuration to device"
    if (getDataValue("MSR")?.startsWith("010F-0C02")){
        response(configure()) //configure device using SmartThings default settings
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
    log.info "Executing zwaveEvent 98 (SecurityV1): 07 (NetworkKeyVerify) with cmd: $cmd (node is securely included)"
    createEvent(name:"secureInclusion", value:"success", descriptionText:"Secure inclusion was successful", isStateChange: true, displayed: true)
    //after device securely joined the network, call configure() to config device
    setSecured()
    log.info "checking this MSR : ${getDataValue("MSR")} before sending configuration to device"
    if (getDataValue("MSR")?.startsWith("010F-0C02")){
        response(configure()) //configure device using SmartThings default settings
    }
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    log.info "Executing zwaveEvent 71 (NotificationV3): 05 (NotificationReport) with cmd: $cmd"
    def result = []
    if (cmd.notificationType == 7) {
        switch (cmd.event) {
            case 0:
                result << createEvent(name: "tamper", value: "clear", displayed: true)
                break
            case 3:
                result << createEvent(name: "tamper", value: "detected", displayed: true, isStateChange: true, descriptionText: "$device.displayName casing was opened")
                break
        }
    } else if (cmd.notificationType == 1) { //Smoke Alarm (V2)
        log.debug "notificationv3.NotificationReport: for Smoke Alarm (V2)"
        result << smokeAlarmEvent(cmd.event)
    }  else if (cmd.notificationType == 4) { // Heat Alarm (V2)
        log.debug "notificationv3.NotificationReport: for Heat Alarm (V2)"
        result << heatAlarmEvent(cmd.event)
    } else {
        log.warn "Need to handle this cmd.notificationType: ${cmd.notificationType}"
        result << createEvent(descriptionText: cmd.toString(), isStateChange: false)
    }
    result
}

def smokeAlarmEvent(value) {
    log.debug "smokeAlarmEvent(value): $value"
    def map = [name: "smoke"]
    if (value == 1 || value == 2) {
        map.value = "detected"
        map.descriptionText = "$device.displayName detected smoke"
    } else if (value == 0) {
        map.value = "clear"
        map.descriptionText = "$device.displayName is clear (no smoke)"
    } else if (value == 3) {
        map.value = "tested"
        map.descriptionText = "$device.displayName smoke alarm test"
    } else if (value == 4) {
        map.value = "replacement required"
        map.descriptionText = "$device.displayName replacement required"
    } else {
        map.value = "unknown"
        map.descriptionText = "$device.displayName unknown event"
    }
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
    log.info "Executing zwaveEvent 84 (WakeUpV1): 07 (WakeUpNotification) with cmd: $cmd"
    def result = [createEvent(descriptionText: "${device.displayName} woke up", displayed: true, isStateChange: true)]
    def cmds = []
    /* check MSR = "manufacturerId-productTypeId" to make sure configuration commands are sent to the right model */
    if (!isConfigured() && getDataValue("MSR")?.startsWith("010F-0C02")) {
        result << response(configure()) // configure a newly joined device or joined device with preference update
    } else {
        //Only ask for battery if we haven't had a BatteryReport in a while
        if (!state.lastbatt || (new Date().time) - state.lastbatt > 24*60*60*1000) {
            log.debug("Device has been configured sending >> batteryGet()")
            cmds << zwave.securityV1.securityMessageEncapsulation().encapsulate(zwave.batteryV1.batteryGet()).format()
            cmds << "delay 1200"
        }
        log.debug("Device has been configured sending >> wakeUpNoMoreInformation()")
        cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()
        result << response(cmds) //tell device back to sleep
    }
    result
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
    log.info "Executing zwaveEvent 5A (DeviceResetLocallyV1) : 01 (DeviceResetLocallyNotification) with cmd: $cmd"
    createEvent(descriptionText: cmd.toString(), isStateChange: true, displayed: true)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.info "Executing zwaveEvent 72 (ManufacturerSpecificV2) : 05 (ManufacturerSpecificReport) with cmd: $cmd"
    log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"
    def result = []
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    updateDataValue("MSR", msr)
    log.debug "After device is securely joined, send commands to update tiles"
    result << zwave.batteryV1.batteryGet()
    result << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01)
    result << zwave.wakeUpV1.wakeUpNoMoreInformation()
    [[descriptionText:"${device.displayName} MSR report"], response(commands(result, 5000))]
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    def result = []
    if (cmd.nodeId.any { it == zwaveHubNodeId }) {
        result << createEvent(descriptionText: "$device.displayName is associated in group ${cmd.groupingIdentifier}")
    } else if (cmd.groupingIdentifier == 1) {
        result << createEvent(descriptionText: "Associating $device.displayName in group ${cmd.groupingIdentifier}")
        result << response(zwave.associationV1.associationSet(groupingIdentifier:cmd.groupingIdentifier, nodeId:zwaveHubNodeId))
    }
    result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.warn "General zwaveEvent cmd: ${cmd}"
    createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

private command(physicalgraph.zwave.Command cmd) {
    if (isSecured()) {
        log.info "Sending secured command: ${cmd}"
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        log.info "Sending unsecured command: ${cmd}"
        cmd.format()
    }
}

private commands(commands, delay=200) {
    log.info "inside commands: ${commands}"
    delayBetween(commands.collect{ command(it) }, delay)
}
private setConfigured(configure) {
    updateDataValue("configured", configure)
}
private isConfigured() {
    getDataValue("configured") == "true"
}
private setSecured() {
    updateDataValue("secured", "true")
}
private isSecured() {
    getDataValue("secured") == "true"
}