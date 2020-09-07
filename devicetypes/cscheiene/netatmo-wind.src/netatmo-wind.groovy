/**
 *  netatmo-windmodule
 *
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
 *     Based on Brian Steere's netatmo-basesatation handler
 *
 */
 
 
metadata {
	definition (name: "Netatmo Wind", namespace: "cscheiene", author: "Brian Steere, cscheiene", mnmn: "SmartThingsCommunity", vid: "787d5e2d-80e1-3f39-bf73-026acafdc48f") {
	    capability "Sensor"
        capability "Battery"
        capability "Refresh"
        capability "Health Check"
		capability "islandtravel33177.wind"
        capability "islandtravel33177.lastUpdate"
        capability "islandtravel33177.gust"
        capability "islandtravel33177.windMaximum"
        capability "islandtravel33177.windMaxTime"
        capability "islandtravel33177.windAngleText"
        capability "islandtravel33177.gustAngleText"
        
        attribute "units", "string"
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
		multiAttributeTile(name:"main", type:"generic", width:6, height:4) {
			tileAttribute("WindStrengthUnits", key: "PRIMARY_CONTROL") {
            	attributeState "default", label:'${currentValue}', icon:"st.Weather.weather1", backgroundColor:"#00a0dc"
            }
            tileAttribute ("WindDirection", key: "SECONDARY_CONTROL") {
				attributeState "WindDirection", label:'Direction: ${currentValue}'
			}
		}        
 		valueTile("GustStrength", "device.GustStrengthUnits", width: 2, height: 1, inactiveLabel: false) {
 			state "default", label:'Gust: ${currentValue}'
 		}
        valueTile("GustDirection", "device.GustDirection", width: 2, height: 1, inactiveLabel: false) {
 			state "default", label:'${currentValue}'            
 		}
        valueTile("max_wind_str", "device.max_wind_strUnits", width: 2, height: 1, inactiveLabel: false) {
 			state "default", label:'Max: ${currentValue}'            
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
 		valueTile("lastupdate", "lastupdate", width: 4, height: 1, inactiveLabel: false) {
            state "default", label:"Last updated: " + '${currentValue}'
            }
         valueTile("windMaxTime", "windMaxTime", width: 2, height: 1, inactiveLabel: false) {
            state "default", label:'${currentValue}'
            }
        standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
 			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
 		}        
        
        main (["main"])
 		details(["main", "GustStrength", "GustDirection","battery", "max_wind_str","windMaxTime" ,"lastupdate","refresh"])
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