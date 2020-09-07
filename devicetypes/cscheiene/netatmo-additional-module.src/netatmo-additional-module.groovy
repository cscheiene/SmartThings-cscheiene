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
	definition (name: "Netatmo Additional Module", namespace: "cscheiene", author: "cscheiene", ocfDeviceType: "oic.d.thermostat", mnmn: "SmartThingsCommunity", vid: "4ee8badb-ce3a-3a50-b333-83ce37eeb19c") {
 		capability "Temperature Measurement"
        capability "Sensor"
        capability "Battery"
		capability "Relative Humidity Measurement"
        capability "Carbon Dioxide Measurement"
        capability "Refresh"
        capability "Health Check"
        capability "islandtravel33177.lastUpdate"
        capability "islandtravel33177.tempTrend"
        
        attribute "min_temp", "number"
        attribute "max_temp", "number"
	}

	simulator {
		// TODO: define status and reply messages here
	}
    preferences {
        input title: "Settings", description: "To change units and time format, go to the Netatmo Connect App", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input title: "Information", description: "Your Netatmo station updates the Netatmo servers approximately every 10 minutes. The Netatmo Connect app polls these servers every 5 minutes. If the time of last update is equal to or less than 10 minutes, pressing the refresh button will have no effect", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input title: "Version ID", description: "070920", displayDuringSetup: false, type: "paragraph", element: "paragraph"
    }    
    

	tiles (scale: 2) {
		multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
            	attributeState "temperature", label:'${currentValue}°', icon:"st.Weather.weather2", backgroundColors:[
 				[value: 31, color: "#153591"],
 				[value: 44, color: "#1e9cbb"],
 				[value: 59, color: "#90d2a7"],
 				[value: 74, color: "#44b621"],
 				[value: 84, color: "#f1d801"],
 				[value: 95, color: "#d04e00"],
 				[value: 96, color: "#bc2323"]
 				]
            }
            tileAttribute ("humidity", key: "SECONDARY_CONTROL") {
				attributeState "humidity", label:'Humidity: ${currentValue}%'
			}           
		} 
 		valueTile("carbonDioxide", "device.carbonDioxide", width: 2, height: 2, inactiveLabel: false) {
 			state "carbonDioxide", label:'${currentValue}ppm', unit:"ppm", backgroundColors: [
 				[value: 600, color: "#44B621"],
                [value: 999, color: "#ffcc00"],
                [value: 1000, color: "#e86d13"]
 				]
 		}
        valueTile("min_temp", "min_temp", width: 2, height: 1) {
 			state "min_temp", label: 'Min: ${currentValue}°'
 		}
        valueTile("max_temp", "max_temp", width: 2, height: 1) {
 			state "max_temp", label: 'Max: ${currentValue}°'
 		}
        valueTile("tempTrend", "tempTrend", width: 4, height: 1) {
 			state "tempTrend", label: 'Temp Trend: ${currentValue}'
 		}         
		valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2) {
			state "battery_percent", label:'Battery: ${currentValue}%', backgroundColors:[
                [value: 20, color: "#ff0000"],
                [value: 35, color: "#fd4e3a"],
                [value: 50, color: "#fda63a"],
                [value: 60, color: "#fdeb3a"],
                [value: 75, color: "#d4fd3a"],
                [value: 90, color: "#7cfd3a"],
                [value: 99, color: "#55fd3a"]
            ]
		}     
		valueTile("temperature", "device.temperature") {
 			state("temperature", label: '${currentValue}°', icon:"st.Weather.weather2", backgroundColors: [
 				[value: 31, color: "#153591"],
 				[value: 44, color: "#1e9cbb"],
 				[value: 59, color: "#90d2a7"],
 				[value: 74, color: "#44b621"],
 				[value: 84, color: "#f1d801"],
 				[value: 95, color: "#d04e00"],
 				[value: 96, color: "#bc2323"]
 				]
 				)
 		}
        valueTile("lastupdate", "lastupdate", width: 4, height: 1, inactiveLabel: false) { 			
            state "default", label:"Last updated: " + '${currentValue}' 		
            }
 		valueTile("date_min_temp", "date_min_temp", width: 2, height: 1, inactiveLabel: false) { 			
          state "default", label:'${currentValue}' 		
          }
        valueTile("date_max_temp", "date_max_temp", width: 2, height: 1, inactiveLabel: false) { 			
          state "default", label:'${currentValue}' 		
          }            
 		standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
 			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
 		}              
               
        main "temperature"
 		details(["temperature", "min_temp","date_min_temp","carbonDioxide", "max_temp","date_max_temp", "tempTrend","lastupdate","battery","refresh"])
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