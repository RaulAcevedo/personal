Then(/^I should see Drive Hours Used are "(.*?)"$/) do |hours|
	macro "I should see \"Drive\" Hours \"Used\" are \"#{hours}\""
end

Then(/^I should see Weekly Hours Used are "(.*)"$/) do |hours|
	macro "I should see \"Weekly\" Hours \"Used\" are \"#{hours}\""
end

Then(/^I should see Duty Hours Used are "(.*)"$/) do |hours|
	macro "I should see \"Duty\" Hours \"Used\" are \"#{hours}\""
end

Then(/^I should see Rest Break Hours Used are "(.*)"$/) do |hours|
	macro "I should see \"Rest Break\" Hours \"Used\" are \"#{hours}\""
end

Then(/^I should see Drive Hours Available are "(.*?)"$/) do |hours|
	macro "I should see \"Drive\" Hours \"Available\" are \"#{hours}\""
end

Then(/^I should see Weekly Hours Available are "(.*)"$/) do |hours|
	macro "I should see \"Weekly\" Hours \"Available\" are \"#{hours}\""
end

Then(/^I should see Duty Hours Available are "(.*)"$/) do |hours|
	macro "I should see \"Duty\" Hours \"Available\" are \"#{hours}\""
end

Then(/^I should see Rest Break Hours Available are "(.*)"$/) do |hours|
	macro "I should see \"Rest Break\" Hours \"Available\" are \"#{hours}\""
end

Then(/^I should see Drive Hours Total are "(.*?)"$/) do |hours|
	macro "I should see \"Drive\" Hours \"Total\" are \"#{hours}\""
end

Then(/^I should see Weekly Hours Total are "(.*)"$/) do |hours|
	macro "I should see \"Weekly\" Hours \"Total\" are \"#{hours}\""
end

Then(/^I should see Duty Hours Total are "(.*)"$/) do |hours|
	macro "I should see \"Duty\" Hours \"Total\" are \"#{hours}\""
end

Then(/^I should see Rest Break Hours Total are "(.*)"$/) do |hours|
	macro "I should see \"Rest Break\" Hours \"Total\" are \"#{hours}\""
end

# More Modular approach, fed by the above macros
# Example: I should see "Drive" Hours "Available" are "3:45:15"
Then(/^I should see "(.*)" Hours "(.*)" are "(.*)"$/) do |inType, inState, inHours|
	# Handle inType arg so that it matches the convention of the Id
	if inType == "Rest Break"
		actualType = "RestBreak"
	else
		actualType = inType
	end

	# Handle inState arg so that it matches the convention of the Id
	if inState == "Available"
		actualState = "Avail"
	elsif inState == "Total"
		actualState = ""
	else
		actualState = inState
	end

	# set ID of text view you are looking for. Example: "tvHoursAvailRestBreak"
	textViewId = "tvHours#{actualState}#{actualType}"
	# query the page looking for the TextView specified and get its text value
	actualHours = query("TextView id:'#{textViewId}'", :text).first

	# compare the returned value of the query to the expected value provided
	unless actualHours == inHours
		fail "#{inType} Hours #{inState} are incorrect. Expected: '#{inHours}' but was '#{actualHours}'."
	end
end 

Given(/^I see my Weekly Hours Used are accurately displaying for my Edit ELD Event Scenario$/) do
	screen_TimeUsed = query("TextView id:'tvHoursUsedWeekly'", :text).first
	screenTimeUsed_inSeconds = convert_Duration_To_Seconds(screen_TimeUsed)

	# Need to add 5 hours of duty time used (from yesterday)
	expectedTimeUsed_inSeconds = @seconds_since_coming_on_duty + (5 * 60 * 60)

	unless (screenTimeUsed_inSeconds >= (expectedTimeUsed_inSeconds - 5)) && (screenTimeUsed_inSeconds <= (expectedTimeUsed_inSeconds + 5))
		print "Expected (in seconds): #{expectedTimeUsed_inSeconds}, Actual: #{screenTimeUsed_inSeconds}"
		fail "Screen time used is not within the 10 second threshold for error when compared to the expected value"
	end
end

Given(/^I see my Weekly Hours Available are accurately displaying for my Edit ELD Event Scenario$/) do
	screen_TimeAvail = query("TextView id:'tvHoursAvailWeekly'", :text).first
	screenTimeAvail_inSeconds = convert_Duration_To_Seconds(screen_TimeAvail)

	# Expected Time Available in seconds = ( 60 hours * 60 minutes * 60 seconds ) - Time used in seconds (which is time since coming on duty + 5 hours from yesterday)
	expectedTimeAvail_inSeconds = (60 * 60 * 60) - (@seconds_since_coming_on_duty + (5 * 60 * 60))

	unless (screenTimeAvail_inSeconds >= (expectedTimeAvail_inSeconds - 5)) && (screenTimeAvail_inSeconds <= (expectedTimeAvail_inSeconds + 5))
		print "Expected (in seconds): #{expectedTimeAvail_inSeconds}, Actual: #{screenTimeAvail_inSeconds}"
		fail "Screen time used is not within the 10 second threshold for error when compared to the expected value"
	end
end

Given(/^I see my Duty Hours Used are accurately displaying for my Edit ELD Event Scenario$/) do
	screen_TimeUsed = query("TextView id:'tvHoursUsedDuty'", :text).first
	screenTimeUsed_inSeconds = convert_Duration_To_Seconds(screen_TimeUsed)

	# This needs to be set when entering the screen
	expectedTimeUsed_inSeconds = @seconds_since_coming_on_duty

	# Compare times, theshold of +/- 5 seconds
	unless (screenTimeUsed_inSeconds >= (expectedTimeUsed_inSeconds - 5)) && (screenTimeUsed_inSeconds <= (expectedTimeUsed_inSeconds + 5))
		print "Expected (in seconds): #{expectedTimeUsed_inSeconds}, Actual: #{screenTimeUsed_inSeconds}"
		fail "Screen time used is not within the 10 second threshold for error when compared to the expected value."
	end
end

Given(/^I see my Duty Hours Available are accurately displaying for my Edit ELD Event Scenario$/) do
	screen_TimeAvail = query("TextView id:'tvHoursAvailDuty'", :text).first
	screenTimeAvail_inSeconds = convert_Duration_To_Seconds(screen_TimeAvail)

	# Expected Time Available in seconds = ( 14 hours * 60 minutes * 60 seconds ) - Time used in seconds
	expectedTimeAvail_inSeconds = (14 * 60 * 60) - @seconds_since_coming_on_duty

	unless (screenTimeAvail_inSeconds >= (expectedTimeAvail_inSeconds - 5)) && (screenTimeAvail_inSeconds <= (expectedTimeAvail_inSeconds + 5))
		print "Expected (in seconds): #{expectedTimeAvail_inSeconds}, Actual: #{screenTimeAvail_inSeconds}"
		fail "Screen time used is not within the 10 second threshold for error when compared to the expected value"
	end
end

Given(/^I see my Rest Break Hours Used are accurately displaying for my Edit ELD Event Scenario$/) do
	screen_TimeUsed = query("TextView id:'tvHoursUsedRestBreak'", :text).first
	screenTimeUsed_inSeconds = convert_Duration_To_Seconds(screen_TimeUsed)

	expectedTimeUsed_inSeconds = @seconds_since_coming_on_duty

	unless (screenTimeUsed_inSeconds >= (expectedTimeUsed_inSeconds - 5)) && (screenTimeUsed_inSeconds <= (expectedTimeUsed_inSeconds + 5))
		print "Expected (in seconds): #{expectedTimeUsed_inSeconds}, Actual: #{screenTimeUsed_inSeconds}"
		fail "Screen time used is not within the 10 second threshold for error when compared to the expected value."
	end
end

Given(/^I see my Rest Break Hours Available are accurately displaying for my Edit ELD Event Scenario$/) do
	screen_TimeAvail = query("TextView id:'tvHoursAvailRestBreak'", :text).first
	screenTimeAvail_inSeconds = convert_Duration_To_Seconds(screen_TimeAvail)

	# Expected Time Available in seconds = ( 8 hours * 60 minutes * 60 seconds ) - Time used in seconds
	expectedTimeAvail_inSeconds = (8 * 60 * 60) - @seconds_since_coming_on_duty

	unless (screenTimeAvail_inSeconds >= (expectedTimeAvail_inSeconds - 5)) && (screenTimeAvail_inSeconds <= (expectedTimeAvail_inSeconds + 5))
		print "Expected (in seconds): #{expectedTimeAvail_inSeconds}, Actual: #{screenTimeAvail_inSeconds}"
		fail "Screen time used is not within the 10 second threshold for error when compared to the expected value"
	end
end

Given(/^I see my Drive Hours Used are accurately displaying for my Edit ELD Event Scenario$/) do
	macro 'I should see Drive Hours Used are "0:00:00"'
end

Given(/^I see my Drive Hours Available are accurately displaying for my Edit ELD Event Scenario$/) do
	macro 'I should see Drive Hours Available are "11:00:00"'
end

Then(/^I calculate seconds since my last Edited Event$/) do 
	# Set instance variable seconds_since_coming_on_duty
	@seconds_since_coming_on_duty = calculate_Time_Since_Event_With_Offset(@last_edited_event_time, @time_offset)
	print "\nSeconds since coming on duty = #{@seconds_since_coming_on_duty}\n"
end

Then(/^I calculate seconds since my remmebered value$/) do
	@seconds_since_coming_on_duty = calculate_Time_Since_Event_With_Offset(@remembered_value, @time_offset)
end

Then(/^I remember my last edited event time for later$/) do
	@remembered_value = @last_edited_event_time
end

# Method to calculate the time since an event when applying the offset value (differnece between ruby's time and device time)
def calculate_Time_Since_Event_With_Offset(startTime, offset)
	# need to apply current date to the time
	currentDate = (Time.now).localtime
	currentDate_formatted = currentDate.strftime("%m/%d/%Y")
	last_event_datetime = "#{currentDate_formatted} #{startTime}"
	# could also do Time.strptime(last_event_datetime, "%D %r") -> %D is date in mm/dd/yyyy and %r is 12-hour time in hh:mm:ss ampm
	last_event_time_parsed = Time.strptime(last_event_datetime, "%m/%d/%Y %I:%M:%S %p")

	currentTime = (Time.now).localtime
	currentTime_after_offset_applied = Time.at(currentTime.to_i + offset.to_i)
	print "\nCurrent Time after Offset Applied = #{currentTime_after_offset_applied}\n"

	#expectedTimeUsed_inSeconds = currentTime_after_offset_applied.to_i - timeOfOnDutyEvent.to_i
	expectedTimeUsed_inSeconds = currentTime_after_offset_applied.to_i - last_event_time_parsed.to_i

	return expectedTimeUsed_inSeconds
end

# Method to convert the read duration value into seconds
def convert_Duration_To_Seconds(timeString)
	timeChunks = timeString.split(":")
	hours = (timeChunks[0]).to_i
	minutes = (timeChunks[1]).to_i
	seconds = (timeChunks[2]).to_i

	time_in_seconds = (hours * 3600) + (minutes * 60) + seconds
	return time_in_seconds
end

