/**
 *  Netatmo Additional Module
 *
 *  Copyright 2019 cscheiene
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "Netatmo Additional Module", namespace: "cscheiene", author: "cscheiene", ocfDeviceType: "oic.d.thermostat", mnmn: "SmartThingsCommunity", vid: "6bf90f50-8d91-30fd-acaa-0a0d9cbb317a") {
 		capability "Temperature Measurement"
        capability "Sensor"
        capability "Battery"
		capability "Relative Humidity Measurement"
        capability "Carbon Dioxide Measurement"
        capability "Refresh"
        capability "Health Check"
        capability "islandtravel33177.lastUpdate"
        capability "islandtravel33177.tempTrend"
        capability "islandtravel33177.minTemp"
        capability "islandtravel33177.maxTemp"
        capability "islandtravel33177.minTempTime"
        capability "islandtravel33177.maxTempTime"
	}

	simulator {
		// TODO: define status and reply messages here
	}
    preferences {
        input title: "Settings", description: "To change units and time format, go to the Netatmo Connect App", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input title: "Information", description: "Your Netatmo station updates the Netatmo servers approximately every 10 minutes. The Netatmo Connect app polls these servers every 5 minutes. If the time of last update is equal to or less than 10 minutes, pressing the refresh button will have no effect", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input title: "Version ID", description: "060221", displayDuringSetup: false, type: "paragraph", element: "paragraph"
    }    
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

}

def poll() {
	log.debug "Polling"
    parent.poll()
}

def refresh() {
    log.debug "Refreshing"
	parent.poll()
}

def installed() {
	sendEvent(name: "checkInterval", value: 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "cloud"])
}

def updated() {
	sendEvent(name: "checkInterval", value: 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "cloud"])
}