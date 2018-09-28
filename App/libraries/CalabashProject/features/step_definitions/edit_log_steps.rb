#steps for the ELD Mandate "Edit Log" screen in RODS > View Log > Edit Log

When(/^I click the Edit button for "(.*)" Status "(.*)"$/) do |status, index|
    # get list of all statuses appearing on screen
    # using "all' gets everything, including ones that aren't displayed"
    listOfStatuses = query("all TextView id:'tvEditLogStatus'", :text)

    counter = 0
    # For loop to check each status line.
    for i in 0..(listOfStatuses.size - 1)
        # if the next status in the array = the expected status, then add 1 to the counter
        if listOfStatuses[i] == status
            counter += 1
        end
        # if you've reached the right number of the chosen status, then save what the index is in the full list of statuses.
        if counter == index.to_i
            actualIndex = i.to_i
            break
        end
    end

    # query the page to get edit button at the expected index
    editButton = query("all Button id:'btnEditLog'")[actualIndex.to_i]
    # create a counter to prevent an infinite loop
    counter = 0
    # While the edit button is not visible, attempt to scroll down. if you scroll 5 times and it's not visible, stop trying.
    while editButton.fetch("visible") == "false" do
        scroll_down
        editButton = query("all Button id:'btnEditLog'")[actualIndex.to_i]
        break if counter == 5
        counter += 1
    end
    
    # touch the edit button
    touch(editButton)
end

# Steps for verifying the data in the ELD Event fields below
Given(/^I see the event Location is "(.*)"$/) do |value|
    macro "I see the field with id of \"editLocation\" has a value of \"#{value}\""
end

Given(/^I see the event Unit Number is "(.*)"$/) do |value|
    macro "I see the field with id of \"editUnitNumber\" has a value of \"#{value}\""
end

Given(/^I see the event Trailer Number is "(.*)"$/) do |value|
    macro "I see the field with id of \"editTrailerNumber\" has a value of \"#{value}\""
end

Given(/^I see the event Shipment Information is "(.*)"$/) do |value|
    macro "I see the field with id of \"editShipmentInfo\" has a value of \"#{value}\""
end

Given(/^I see the event Drivers Annotation is "(.*)"$/) do |value|
    macro "I see the field with id of \"editDriversAnnotation\" has a value of \"#{value}\""
end

Given(/^I see the field with id of "(.*)" has a value of "(.*)"$/) do |id, value|
    actual = query("all EditText id:'#{id}'", :text).first
    unless actual == value
        fail "Field with id: #{id} does not contain correct text. Expected: #{value}, Actual: #{actual}"
        #print "Field with id: #{id} does not contain correct text. Expected: #{value}, Actual: #{actual}"
    end
end

#steps for editing ELD Mandate ELD Events below

When(/^I set the event Location to "(.*)?"$/) do |loc|
    macro "I set the event field with id of \"editLocation\" to a value of \"#{loc}\""
end

When(/^I set the event Unit Number to "(.*)?"$/) do |unit|
    macro "I set the event field with id of \"editUnitNumber\" to a value of \"#{unit}\""
end

When(/^I set the event Trailer Number to "(.*)?"$/) do |tNum|
    macro "I set the event field with id of \"editTrailerNumber\" to a value of \"#{tNum}\""
end

When(/^I set the event Shipment Information to "(.*)?"$/) do |info|
    macro "I set the event field with id of \"editShipmentInfo\" to a value of \"#{info}\""
end

When(/^I set the event Drivers Annotation to "(.*)?"$/) do |note|
    macro "I set the event field with id of \"editDriversAnnotation\" to a value of \"#{note}\""
end

# Modular
When(/^I set the event field with id of "(.*)" to a value of "(.*)?"$/) do |id, value|
    # check to see if you need to scroll down
    if (element_exists("all EditText id:'#{id}'") && !element_exists("EditText id:'#{id}'"))
        scroll_down
    end
    unless element_exists("EditText id:'#{id}'")
        fail "Editable Text Field with id \"#{id}\" does not exist"
    end
    clear_text_in("EditText id:'#{id}'")
    touch("EditText id:'#{id}'")
    keyboard_enter_text value
    hide_soft_keyboard
end

Given(/^I see the error text "(.*)" on the Edit ELD Event screen$/) do |errorMessage|
    wait_for(5){element_exists("TextView id:'txtError'")}
    message = query("TextView id:'txtError'", :text).first
    if message.nil? || message.empty?
        fail "Unfortunately, no error message is currently displayed"
    end
    unless message.gsub(/\s+/, '') == errorMessage.gsub(/\s+/, '')
        fail "Error message does not match expected value. Expected: #{errorMessage}, Actual: #{message}"
    end
end 

Given(/^I see the success text "(.*)" on the Edit ELD Event screen$/) do |successMessage|
    wait_for(5){element_exists("TextView id:'txtSuccess'")}
    message = query("TextView id:'txtSuccess'", :text).first
    if message.nil? || message.empty?
        fail "No success message is being displayed"
    end
    unless message == successMessage
        fail "Success message does not match expected value. Expected: #{successMessage}, Acutal: #{message}"
    end
end

When(/^I set the Start Time later$/) do
    macro "I set the \"Start\" Time later"
end

When(/^I set the End Time later$/) do
    macro "I set the \"End\" Time later"
end

When(/^I set the "(.*)" Time later$/) do |start_or_end|
    # get the original time value of the event
    originalTime = query("Button id:'btn#{start_or_end}Time'", :text).first
    
    # split the time string apart for easier manipulation
    timeChunks = originalTime.split(":")

    # break time down into hours, minutes, seconds, and AM/PM
    hours = timeChunks[0]
    minutes = timeChunks[1]
    seconds = timeChunks[2].split(" ")[0]

    # Get AM/PM from the last "chunk" which should look like 'ss AMPM'
    ampm = timeChunks[2].split(" ")[1]

    # check to see if adding 5 to minutes changes the hour
    if minutes.to_i >= 55
        # since adding 5 changes the hour, just advance the hour by 1
        # check to see if adding an hour is possible
        # if 11:55 PM or later, then cannot add time, so fail
        if hours == 11 && (ampm.to_s).casecmp("pm").zero?
            fail "Unable to add 5 minutes, as there is not enough time left in the day"
        else
            # if 12pm, then change to 1pm
            if hours == 12
                hours == 1
            # if anything but 12 PM, add 1 to the hours
            else
                hours = hours.to_i + 1
                # if changing from 11 to 12, then change to PM
                if hours.to_i == 12
                    ampm = "pm"
                end
                # since time is 55-59, add 5 minutes, then subtract 60 (for new hour)
                minutes = minutes.to_i - 55
            end
        end
    # if less that 55 minutes into the hour, simply add 5 minutes
    else
        minutes = minutes.to_i + 5
    end

    # rebuild the time
    newTime = "#{hours}:#{minutes}:#{seconds} #{ampm}"

    # now set the actual time
    macro "I set the #{start_or_end} Time of the event to \"#{newTime}\""
end

When(/^I set the Start Time earlier$/) do
    macro 'I set the "Start" Time earlier'
end

When(/^I set the End Time earlier$/) do
    marco 'I set the "End" Time earlier'
end

When(/^I set the "(.*)" Time earlier$/) do |start_or_end|
    # get the original time value of the event
    originalTime = query("Button id:'btn#{start_or_end}Time'", :text).first
     
    # split the time string apart for easier manipulation
    timeChunks = originalTime.split(":")
 
    # break time down into hours, minutes, seconds, and AM/PM
    hours = timeChunks[0]
    minutes = timeChunks[1]
    seconds = timeChunks[2].split(" ")[0]
 
    # Get AM/PM from the last "chunk" which should look like 'ss AMPM'
    ampm = timeChunks[2].split(" ")[1]

    # Try to subtract 5 minutes from the time
    # if there are less than 5 minutes left, handle it
    if minutes.to_i < 5
        # if it's midnight, we can't go back to yesterday, so fail
        if hours.to_i == 12 && ampm.casecmp("am").zero?
            fail "Subtracting 5 minutes is not possible, as it would move the time to the previous day"
        # if it's not midnight, we can subtract an hour
        else
            # if the hour is 1, we need to change it to 12
            if hours.to_i == 1
                hours = 12
            else
                hours = hours.to_i - 1
                # Handle switching from 12PM to 11AM note: 12AM to 11PM is not possible, see fail message above
                if hours.to_i == 11
                    ampm = "AM"
                end
            end
            # subtract 5 minutes, then add 60 (for new hour)
            minutes = minutes.to_i + 55
        end
    # if there are 5 or more minutes remaining, subtract 5
    else
        minutes = minutes.to_i - 5
    end

    # account for values less than 10
    hours_str = add_zeros_to_time_if_necessary(hours)
    minutes_str = add_zeros_to_time_if_necessary(minutes)
    seconds_str = add_zeros_to_time_if_necessary(seconds)

    # rebuild the time string
    newTime = "#{hours_str}:#{minutes_str}:#{seconds_str} #{ampm}"

    # set the time
    macro "I set the #{start_or_end} Time of the event to \"#{newTime}\""
end

def add_zeros_to_time_if_necessary (input)
    s = input.to_i
    output = "#{s}"

    if s < 10
        # add a zero
        output = "0#{output}"
    end

    return output
end

# note: time must be passed in as hh:mm:ss AMPM
Then(/^I set the Start Time of the event to "(.*)"$/) do |startTime|
    macro "I set the \"Start\" Time of the event to \"#{startTime}\""
end

Then(/^I set the End Time of the event to "(.*)"$/) do |endTime|
    macro "I set the \"End\" Time of the event to \"#{endTime}\""
end

# Used for setting both Start and End times
Then(/^I set the "(.*)" Time of the event to "(.*)"$/) do |start_or_end, expectedTime|
    sleep(1)
    btnId = "btn#{start_or_end}Time"

    timeButton = query("button id:'#{btnId}'").first
    unless timeButton.fetch("enabled") == true
        fail "#{start_or_end} Time is disabled. You cannot edit it."
    end

    # get the original start time
    @last_edited_event_original_time = query("button id:'#{btnId}'", :text).first

    touch(timeButton)
    timeChunks = expectedTime.split(":")
    # break time down into hours, minutes, seconds, and AM/PM
    hours = timeChunks[0]
    minutes = timeChunks[1]
    seconds = timeChunks[2].split(" ")[0]
    # Get AM/PM from the last "chunk" which should look like 'ss AMPM'
    ampm = timeChunks[2].split(" ")[1]

    # Get sections
    btns = query("all CustomEditText id:'numberpicker_input'")
    hourBtn = btns[0]
    minuteBtn = btns[1]
    secondsBtn = btns[2]

    # set Hours
    #touch("NumberPicker id:'hour'")
    touch(hourBtn)
    keyboard_enter_text hours
    hide_soft_keyboard

    # set Minutes
    #touch("NumberPicker id:'minute'")
    touch(minuteBtn)
    keyboard_enter_text minutes
    hide_soft_keyboard

    # set Seconds
    #touch("NumberPicker id:'seconds'")
    touch(secondsBtn)
    keyboard_enter_text seconds
    hide_soft_keyboard

    # set AM/PM
    # since AM/PM is a button, and not a field to select, compare the actual to expected and then adjust if necessary
    currentAMPM = query("Button id:'amPm'", :text).first
    if currentAMPM.casecmp(ampm) == false
        touch("Button id:'amPm'")
    end

    # click OK
    touch("Button text:'OK'")

    # Set an instance variable to the event time
    @last_edited_event_time = expectedTime
end

Given(/^I see the End Time is disabled$/) do
    unless query("Button id:'btnEndTime'", :enabled).first == false
        fail "End Time is enabled, but we expected it to be disabled."
    end
end

Given(/^I see the Start Time is disabled$/) do
    unless query("Button id:'btnStartTime'", :enabled).first == false
        fail "Start Time is enabled, but we expected it to be disabled."
    end
end

When(/^I set the event Duty Status to "(.*)"$/) do |status|
    touch("Spinner id:'cboDutyStatus'")
    wait_for(60){element_exists("DialogTitle id:'alertTitle'")}
    touch("CheckedTextView text:'#{status}'")
end

When(/^I calculate the time offset$/) do
    macro "I choose menu item Menu"
    # get current time
    currentTime = (Time.now).localtime
    # click New Status screen
    macro "I choose menu item New Status"
    # read displayed time
    screenTime_raw_pieces = (query("TextView id:'rns_lbltime'",:text).first).split(" ")
    t = screenTime_raw_pieces[1]
    ampm = screenTime_raw_pieces[2]
    screenTime_raw = "#{t} #{ampm}"
    #print "The raw screen time is: #{screenTime_raw}"
    screenTime = Time.parse(screenTime_raw, currentTime)

    # calculate offset
    offset = screenTime.to_i - currentTime.to_i

    # format original
    formattedTime = currentTime.strftime("%I:%M:%S %p")
    #print "\nThe Current Time is: #{formattedTime}"
    
    # format screen
    formattedScreenTime = screenTime.strftime("%I:%M:%S %p")
    #print "\nThe Screen Time is: #{formattedScreenTime}"
    # return to RODS
    scroll_down
    touch("Button text:'Cancel'")
    # display offset
    #print "\nThe Offset is: #{offset} seconds"
    # save the offset to an instance variable
    @time_offset = offset
end

When(/^I switch to the previous days log$/) do
    touch("Button id:'btnPreviousDay'")
end

When(/^I switch to the next days log$/) do
    touch("Button id:'btnNextDay'")
end