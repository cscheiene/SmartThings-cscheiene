/**
 *  Copyright 2017 Tibber
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
 *  Tibber price
 *
 *  Author: tibberdev,cscheiene
 */
metadata {
	definition (name: "Tibber Price", namespace: "cscheiene", author: "tibberdev, cscheiene", mnmn: "SmartThingsCommunity", vid: "560e33c3-94b5-3b05-970c-de5af9ae5ae9", ocfDeviceType: "x.com.st.d.energymeter") {
		capability "Sensor"
        capability "Refresh"
		capability "islandtravel33177.tibberPriceNextHour"
        capability "islandtravel33177.tibberPrice"
        capability "islandtravel33177.tibberPriceMaxDayLabel"
        capability "islandtravel33177.tibberPriceMaxDay"
        capability "islandtravel33177.tibberPriceMinDay"
        capability "islandtravel33177.tibberPriceMinLabel"
        capability "islandtravel33177.tibberPricePlusTwoHour"
        capability "islandtravel33177.tibberPriceLevel"
        capability "islandtravel33177.tibberConsumptionPrevHour"
        capability "islandtravel33177.tibberPriceNextHourDisplay"
        capability "islandtravel33177.tibberPricePlusTwoHourDisplay"
        capability "islandtravel33177.tibberPriceMinDayDisplay"
        capability "islandtravel33177.tibberPriceMaxDayDisplay"
        capability "islandtravel33177.tibberAddress"
		capability "Energy Meter" //workaround for Actions tiles etc
		
		attribute "currency", "string"
	}

    preferences {
        input (
            name: "tibber_apikey",
            type: "password",
            title: "API Key",
            description: "Enter the Tibber API key",
            required: true,
            displayDuringSetup: true
        )
        input (
            name: "apkeylink",
            type: "paragraph",
            title: "API Key can be found here:",
            description: "https://developer.tibber.com/settings/accesstoken"
        )
		input (
            name: "NORMAL",
            type: "paragraph",
            title: "NORMAL",
            description: "The price is greater than 90 % and smaller than 115 % compared to average price."
        )
        input (
            name: "CHEAP",
            type: "paragraph",
            title: "CHEAP",
            description: "The price is greater than 60 % and smaller or equal to 90 % compared to average price."
        )
        input (
            name: "VERY CHEAP",
            type: "paragraph",
            title: "VERY CHEAP",
            description: "The price is smaller or equal to 60 % compared to average price."
        )
        input (
            name: "EXPENSIVE",
            type: "paragraph",
            title: "EXPENSIVE",
            description: "The price is greater or equal to 115 % and smaller than 140 % compared to average price."
        )
        input (
            name: "VERY EXPENSIVE",
            type: "paragraph",
            title: "VERY EXPENSIVE",
            description: "The price is greater or equal to 140 % compared to average price."
        )
        input (
            name: "VERSION",
            type: "paragraph",
            title: "Version number",
            description: "110421"
        )
        input (
            name: "home",
            type: "number",
            title: "Home",
            description: "Enter the home you want to display, default is 0",
            required: true,
            displayDuringSetup: true,
        )
    }
}

def homeNumber() {
		if(settings.home == null){
        return 0
        } else {
        return settings.home}
}        


def initialize() {
	state.price = 100;
	log.debug("init")
    getPrice()
    schedule("0 1,5,10,15,20,25,30 * * * ?", getPrice)
}

def installed() {
	log.debug "Installed"
    initialize()
}

def ping() {
    refresh()
}

def updated() {
	log.debug "Updated"
    initialize()
}

def refresh() {
    initialize()
}

def getPrice() {
	log.debug("getPrice")
    if(tibber_apikey == null){
        log.error("API key is not set. Please set it in the settings.")
    } else {
        def params = [
            uri: "https://api.tibber.com/v1-beta/gql",
            headers: ["Content-Type": "application/json;charset=UTF-8" , "Authorization": "Bearer $tibber_apikey"],
            body: graphQLApiQuery()
        ]
        try {
            httpPostJson(params) { resp ->
                if(resp.status == 200){
                    def today = resp.data.data.viewer.homes[homeNumber()].currentSubscription.priceInfo.today
                    def tomorrow = resp.data.data.viewer.homes[homeNumber()].currentSubscription.priceInfo.tomorrow

                    
                    def price = Math.round(resp.data.data.viewer.homes[homeNumber()].currentSubscription.priceInfo.current.total * 100)
                    def priceMaxDay = Math.round(MaxValue(today) *100)
                    def priceMaxDayLabel = "${MaxValueTimestamp(today)}:00"
                    def priceMinDay = Math.round(MinValue(today) *100)
                    def priceMinDayLabel = "${MinValueTimestamp(today)}:00"

                    def priceList = today
                    tomorrow.each{
                        priceList << it
                    }
                    def priceNextHours = PriceNextHours(priceList)
                    def priceNextHour = Math.round(priceNextHours[0] *100)
                    def priceNextHourLabel = "@ ${priceNextHours[2]}"
                    def pricePlusTwoHour = Math.round(priceNextHours[1] *100)
                    def pricePlusTwoHourLabel = "@ ${priceNextHours[3]}"
                    def currency = resp.data.data.viewer.homes[homeNumber()].currentSubscription.priceInfo.current.currency
                    def level = resp.data.data.viewer.homes[homeNumber()].currentSubscription.priceInfo.current.level
                    def consumptionPrevHour = resp.data.data.viewer.homes[homeNumber()].consumption.nodes[0].consumption
                    def address = resp.data.data.viewer.homes[homeNumber()].address.address1
                    
                    
                    

                    currency = "${currency}: ${currencyToMinor(currency)}/kWh"

                    state.currency = currency
                    state.level = level
                    state.price = price
                    state.priceNextHour = priceNextHour
                    state.priceNextHourLabel = priceNextHourLabel
                    state.pricePlusTwoHour = pricePlusTwoHour
                    state.pricePlusTwoHourLabel = pricePlusTwoHourLabel
                    state.priceMaxDay = priceMaxDay
                    state.priceMaxDayLabel = priceMaxDayLabel
                    state.priceMinDay = priceMinDay
                    state.priceMinDayLabel = priceMinDayLabel

                    sendEvent(name: "consumptionPrevHour", value: consumptionPrevHour, unit: "kWh")
                    sendEvent(name: "address", value: address)
                    sendEvent(name: "energy", value: price, unit: currency)
                    sendEvent(name: "price", value: state.price, unit: currency)
                    sendEvent(name: "priceNextHour", value: state.priceNextHour, unit: currency)
                    sendEvent(name: "pricePlusTwoHour", value: state.pricePlusTwoHour, unit: currency)
                    sendEvent(name: "priceMaxDay", value: state.priceMaxDay, unit: currency)
                    sendEvent(name: "priceMinDay", value: state.priceMinDay, unit: currency)
                    sendEvent(name: "priceMinDayDisplay", value: "$priceMinDay $currency @ $priceMinDayLabel")
                    sendEvent(name: "priceMaxDayDisplay", value: "$priceMaxDay $currency @ $priceMaxDayLabel")

                    sendEvent(name: "priceNextHourDisplay", value: "$priceNextHour $currency $priceNextHourLabel")
                    sendEvent(name: "pricePlusTwoHourDisplay", value: "$pricePlusTwoHour $currency $pricePlusTwoHourLabel")
                    sendEvent(name: "priceMaxDayLabel", value: state.priceMaxDayLabel)
                    sendEvent(name: "priceMinDayLabel", value: state.priceMinDayLabel)

                    sendEvent(name: "currency", value: state.currency)
                    sendEvent(name: "level", value: state.level)
                    
                    
                    log.debug "$address"
                }
            }
        } catch (e) {
            log.debug "something went wrong: $e"
        }
    }
}
def parse(String description) {
    log.debug "parse description: ${description}"
    def eventMap = [
        createEvent(name: "energy", value: state.price, unit: state.currency)
        ,createEvent(name: "level", value: state.level)
        ,createEvent(name: "price", value: state.price, unit: state.currency)
        ,createEvent(name: "priceNextHour", value: state.priceNextHour, unit: state.currency)
        ,createEvent(name: "pricePlusTwoHour", value: state.pricePlusTwoHour, unit: state.currency)
        ,createEvent(name: "priceMaxDay", value: state.priceMaxDay, unit: state.currency)
        ,createEvent(name: "priceMinDay", value: state.priceMinDay, unit: state.currency)
        ,createEvent(name: "priceNextHourLabel", value: state.priceNextHourLabel)
        ,createEvent(name: "pricePlus2HourLabel", value: state.pricePlus2HourLabel)
        ,createEvent(name: "priceMaxDayLabel", value: state.priceMaxDayLabel)
        ,createEvent(name: "priceMinDayLabel", value: state.priceMinDayLabel)    
        ,createEvent(name: "currencyLabel", value: state.currency, unit: state.currency)   
    ]
    log.debug "Parse returned ${description}"
    return eventMap
}

def currencyToMinor(String currency){
	def currencyUnit = "";
	switch(currency){
    	case "NOK":currencyUnit = "Øre";break;
        case "SEK":currencyUnit = "Øre";break;
        case "USD":currencyUnit = "Penny";break;
        default: currencyUnit = "";break;
    }
    return currencyUnit;
    
}

def graphQLApiQuery(){
    return '{"query": "{viewer {homes {address{address1} consumption(resolution: HOURLY, last: 1) {nodes {consumption consumptionUnit}} currentSubscription {priceInfo { current {total currency level} today {total startsAt} tomorrow{ total startsAt }}}}}}", "variables": null, "operationName": null}';
}

def MaxValueTimestamp(List values){
	def max = 0
    def maxTimestamp = ""
	values.each{
    	def timestamp = it.startsAt
        def total = it.total
        if(total>max){
        	max = it.total
            maxTimestamp = timestamp
        }
    }
    return maxTimestamp.substring(11,13)
}
def MaxValue(List values){
	def max = 0
    def maxTimestamp = ""
	values.each{
    	def timestamp = it.startsAt
        def total = it.total
        if(total>max){
        	max = it.total
            maxTimestamp = timestamp
        }
    }
    return max
}

def MinValueTimestamp(List values){
	def min = 1000
    def minTimestamp = ""
	values.each{
    	def timestamp = it.startsAt
        def total = it.total   
        if(it.total<min){
        	min = it.total
            minTimestamp = timestamp
        }
    }
    return minTimestamp.substring(11,13)
}
def MinValue(List values){
	def min = 1000
    def minTimestamp = ""
	values.each{
    	def timestamp = it.startsAt
        def total = it.total   
        if(it.total<min){
        	min = it.total
            minTimestamp = timestamp
        }
    }
    return min
}


def PriceNextHours(List values){
	def priceNowTimestamp = 0
    def priceNextHour = -1;
    def priceNextNextHour = -1;
    def i=0
    values.each{
        Calendar cal=Calendar.getInstance();
        def hourNowUtc = cal.get(Calendar.HOUR_OF_DAY) + 1
        def dayNowUtc = cal.get(Calendar.DAY_OF_MONTH)    
        def startsAt = it.startsAt
        def total = it.total        
        int hourNow = startsAt.substring(11,13) as int
        int dayNow = startsAt.substring(8,10) as int
        int hourOffset = startsAt.substring(20,22) as int
        def timeZoneOperator = startsAt.substring(19,20)
        if(timeZoneOperator=="+"){
            hourNowUtc = hourNowUtc + hourOffset
        }
        if(timeZoneOperator=="-"){
            hourNowUtc = hourNowUtc - hourOffset
        }
        if(hourNowUtc<0){
        	hourNowUtc = hourNowUtc+24 //wrap
            dayNowUtc = dayNowUtc-1
        }
        if(hourNowUtc>23){
        	hourNowUtc = hourNowUtc-24 //wrap
            dayNowUtc = dayNowUtc+1
        }
        if(hourNow == hourNowUtc && dayNow == dayNowUtc ){
        	priceNextHour = it.total
            priceNextNextHour = values[i+1].total   
            priceNowTimestamp = hourNowUtc
        }
    	i++

    }
    
    def priceNextTimestamp = 0
    if(priceNowTimestamp<23)
    	priceNextTimestamp = priceNowTimestamp + 1
    return [priceNextHour, priceNextNextHour, fromToTimestamp(priceNowTimestamp), fromToTimestamp(priceNextTimestamp)]
}
def fromToTimestamp(def timestamp){
	def from = timestamp
    def to = timestamp + 1
    if(to>23){
    	to = 0
    }
    return "${formatTimestamp(from)} - ${formatTimestamp(to)} "
}
def formatTimestamp(def timestamp){
	if(timestamp < 9)
    	return "0${timestamp}"
    return timestamp
}