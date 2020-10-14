/**
 *  Copyright 2014 SmartThings
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
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Simulated Presence Sensor On/Off", namespace: "cscheiene", author: "cscheiene", mnmn: "SmartThingsCommunity", vid: "bcb8b7aa-a8f6-32c3-90d9-add17e686d27") {
		capability "Presence Sensor"
		capability "Sensor"
        capability "Actuator"
		capability "Switch"
        capability "Occupancy Sensor"

		command "arrived"
		command "departed"
	}

	simulator {
		status "present": "presence: present"
		status "not present": "presence: not present"
	}

	tiles {
		standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state("not present", label:'away', icon:"st.presence.tile.not-present", backgroundColor:"#ffffff", action:"arrived")
			state("present", label:'present', icon:"st.presence.tile.present", backgroundColor:"#00A0DC", action:"departed")
		}
		main "presence"
		details "presence"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}

// handle commands
def arrived() {
	log.trace "Executing 'arrived'"
	sendEvent(name: "presence", value: "present")
    sendEvent(name: "switch", value: "on")
    sendEvent(name: "occupancy", value: "occupied")
}


def departed() {
	log.trace "Executing 'arrived'"
	sendEvent(name: "presence", value: "not present")
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "occupancy", value: "unoccupied")
}

def on() {
	log.trace "Switch on, executing 'arrived'"
	sendEvent(name: "presence", value: "present")
    sendEvent(name: "switch", value: "on")
    sendEvent(name: "occupancy", value: "occupied")
}

def off() {
	log.trace "Switch off, executing 'arrived'"
	sendEvent(name: "presence", value: "not present")
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "occupancy", value: "unoccupied")
}