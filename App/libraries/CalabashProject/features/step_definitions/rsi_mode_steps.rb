Given(/^I see options for Data Transfer and Roadside Inspection Mode$/) do
	wait_for(30) {element_exists("Button text:'Roadside Inspection Mode'")}
    check_element_exists("Button text:'Roadside Inspection Mode'")
    check_element_exists("Button text:'Data Transfer'")
end

Then(/^I enter "(.*)" as my logon password$/) do |password|
    clear_text_in("EditText id:'txtpassword'")
    touch("EditText id:'txtpassword'")
    keyboard_enter_text password
    hide_soft_keyboard
end

Then(/^I press the Roadside Inspection Mode button$/) do
    touch("Button text:'Roadside Inspection Mode'")
end

Given(/^I see the error text "(.*)" on the RSI mode login screen$/) do |expected_message|
    wait_for(5){element_exists("TextView id:'message'")}
    actual_message = query("textView id:'message'", :text).first
    if actual_message.nil? || actual_message.empty?
        fail "Unfortunately, no error message is currently displayed"
    end
    unless actual_message.gsub(/\s+/, '') == expected_message.gsub(/\s+/, '')
        fail "Error message does not match expected value. Expected: #{expected_message}, Actual: #{actual_message}"
    end
end

Then(/^I dismiss the RSI login error prompt$/) do
    touch("Button id:'button1'")
    puts "tapped".blue
    wait_for(2){element_does_not_exist("TextView id:'message'")}
end

Given(/^I see the ELD Mandate RSI Mode screen$/) do
    wait_for(5){element_exists("TextView text:'Roadside Inspection Mode ELD'")}
end

Then(/^I click the "(<|>)" key to view the (previous|next) day$/) do |arrow, direction|
    touch("Button text:'#{arrow}'")
end

Then(/^I click the Exit RSI Mode button$/) do
    touch("CheckedTextView text:'Exit Roadside Inspection Mode'")
end

Given(/^I see the "(.*)" is "(.*)"$/) do |field_name, value|
   field_id = case field_name
    when "Driver ID"
        "driver_id"
    when "Driver Name"
        "driver_name"
    when "Data Diagnostic Status"
        "data_diagnostic_status"
    when "ELD Malfunctions"
        "eld_malfunction_status"
    when "ELD Provider"
        "eld_provider"
    when "ELD ID"
        # ELD ID varies based on the ELD generation and tablet type in use. This is managed by data set in DMO admin. This data is volatile, so I don't suggest you use it at this time.
        "eld_id"
    when "Truck Vehicle VIN"
        puts "Truck Vehicle VIN won't work until PBI 59136 is complete. Setting as blank".red
        value = ""
        "truck_tractor_vin"
    when "Truck Vehicle ID"
        if value.casecmp("#MYELD#").zero?
            value = ENV['UNIT_ID']
        end
        "truck_tractor_id"
    when "Engine Hours"
        "engine_hours"
    when "Current Odometer"
        "current_odometer"
    when "Shipping ID"
        "shipping_id"
    when "Time Zone"
        "time_zone"
    when "Exempt Driver Status"
        "exempt_driver_status"
    when "Unidentified Driving Records"
        "unidentified_driving_records"
    when "Current Location"
        "current_location"
    when "Co-Driver ID"
        "co_driver_id"
    when "Co-Driver"
        "co_driver"
    when "License Number"
        "license_number"
    when "License State"
        "license_state"
    when "Date of Record"
        if value.casecmp("today").zero?
            value = format_date(get_today)
        elsif value.casecmp("yesterday").zero?
            value = format_date(get_yesterday)
        end
        "date_of_record"
    when "24 Hour Start Time"
        "log_start_time"
    when "Carrier"
        "carrier"
    when "Date of Display"
        if value.casecmp("today").zero?
            value = format_date(get_today)
        elsif value.casecmp("yesterday").zero?
            value = format_date(get_yesterday)
        end
        "date_of_display"
    when "Miles Today"
        "miles_today"
    when "Total Time in Working Day So Far"
        "total_hours_in_working_day_so_far"
    end

    scroll_up_until_visible("TextView id:'#{field_id}'", 5)
    scroll_down_until_visible("TextView id:'#{field_id}'", 5)
    actual_value = query("TextView id:'#{field_id}'", :text).first
    unless actual_value == ("#{field_name}: " + value)
        fail "#{field_name} did not match expected value. Expected: #{field_name}: #{value}, Actual: #{actual_value}"
    end
end

Given(/^I see the ELD ID is not blank$/) do
    scroll_up_until_visible("TextView id:'eld_id'", 5)
    scroll_down_until_visible("TextView id:'eld_id'", 5)
    if (query("TextView id:'eld_id'", :text).first).empty?
        fail "ELD ID is not populated. A value was expected"
    end
end

Given(/^I see the date "(.*)" is displayed in the dropdown$/) do |date|
    if date.casecmp("today").zero?
        date = format_date(get_today)
    end

    if date.casecmp("yesterday").zero?
        date = format_date(get_yesterday)
    end

    displayed_date = query("TextView id:'text1'", :text).first

    date_parts = displayed_date.split("/")
    year = "20" + date_parts[2]
    month = date_parts[0]
    day = date_parts[1]
    adjusted_displayed_date = format_date(Date.new(year.to_i, month.to_i, day.to_i))

    unless date == adjusted_displayed_date
        fail "Date does not match expected. Expected: #{date}. Actual: #{adjusted_displayed_date}"
    end
end

def scroll_down_until_visible(query, max_scrolls)
    sleep(1)
    obj = query("all " + query)
    if obj.empty?
        fail "could not find object with query \"" + query + "\""
    end
    obj = obj.first
    counter = 0
    while (obj.fetch("visible") == false && counter < max_scrolls) do
        puts "Visible: #{obj.fetch("visible")}"
        scroll_down
        sleep(1)
        obj = query("all " + query).first
        counter += 1
        puts "object is not on-screen, scrolling down"
    end
end

def scroll_up_until_visible(query, max_scrolls)
    sleep(1)
    obj = query("all " + query)
    if obj.empty?
        fail "could not find object with query \"" + query + "\""
    end
    obj = obj.first
    counter = 0
    while (obj.fetch("visible") == false && counter < max_scrolls) do
        puts "Visible: #{obj.fetch("visible")}"
        scroll_up
        sleep(1)
        obj = query("all " + query).first
        counter += 1
        puts "object is not on-screen, scrolling up"
    end
end

def get_today
    return (Time.now).localtime
end

def format_date(datetime)
    # formats as 3/4/2005 instead of 03/04/2005
    return datetime.strftime("%m/%d/%Y")
end

def get_yesterday
    today = get_today
    return Time.at(today.to_i - (24*60*60))
end

Then(/^I read the rows from the table$/) do
    output_rows_to_text(read_rows_from_event_table)
end
# Read rows of data from table (output below)
# pattern = Logdate => Time, Event Type, Code, Sequence Id, Record Status (missing for some), Origin, Location (missng for some), Odometer, Engine Hours, Comment (missing for some)
def read_rows_from_event_table
    # declare array for rows
    days = []
    rows = []
    row_data = []
    all_data = query("all TextView")
    all_data.each do |obj|
        if obj.fetch("id") == 'tvDate'
            # This is new grouping of events due to day
            if rows.length > 0
                # new day
                #puts "New Day. Rows length > 0\nId = #{obj.fetch('id')} Text = #{obj.fetch('text')}".blue
                # ensure we push the last row of the previous day
                if(row_data.length > 0)
                    rows.push(row_data.dup)
                end
                days.push(rows.dup)
                rows = []
            end
        else
            if obj.fetch("id") == 'tvTime'
                # This is the start of a new row
                # push old row into rows array
                if(row_data.length > 0)
                    rows.push(row_data.dup)
                end
                # reset row data
                row_data = []
                row_data.push(obj.fetch("text"))
            elsif ["tvEventType", "tvEventCode", "tvSequenceId", "tvRecordStatus", "tvOrigin", "tvLocation", "tvOdometerCalibration", "tvEngineHours", "tvComment"].include?(obj.fetch("id"))
                # add info to row data    
                row_data.push(obj.fetch("text"))

                #puts "Adding Id = #{obj.fetch('id')} with Text = #{obj.fetch('text')} to Day #{days.length}".blue
            else
                # text view is not part of the table. Skip it!
                puts "TextView is not part of table!"
            end
        end
    end
    # ensure you push the last row
    rows.push(row_data.dup)
    # push rows into day
    days.push(rows.dup)
    screenshot({:name=> "table.png"})
    # return array of rows
    #return rows
    return days[0]
end

def output_rows_to_text(rows)
    rows.each do |r|
        row_text = ""
        r.each do |cell|
            row_text += " | " + cell
        end
        puts (row_text + " | ").blue
        puts "******"
    end
end

Then(/^I verify my table data for RSI Scenario Team Driver 2$/) do
    scroll_down
    scroll_down
    scroll_down
    row_0_data = ["00:00:00", "1", "1", "2", "1", "1", "Fake Location"] #midnight
    row_1_data = ["08:15:00", "1", "4", "3", "1", "1", "Fake Location"] #on duty
    row_2_data = ["08:15:31", "5", "1", "1", "", "1", ""] #login
    row_3_data = ["09:00:00", "3", "2", "4", "1", "1", "Fake Location"] #ym start
    row_4_data = ["09:30:00", "3", "0", "5", "1", "1", "Fake Location"] #ym end
    row_5_data = ["09:45:00", "5", "2", "6", "", "1", ""] #logout
    row_6_data = ["09:45:00", "1", "1", "7", "1", "1", "Fake Location"] #Off Duty
    puts "Defects on the RSI mode screen are causing Record Status and Location to be missing from non-duty status events".red

    compare = [row_0_data, row_1_data, row_2_data, row_3_data, row_4_data, row_5_data, row_6_data]
    rows = read_rows_from_event_table

    compare_rsi_ui_event_table_data(compare, rows)
end

Then(/^I verify my table data for RSI Scenario Team Driver 1$/) do 
    scroll_down
    scroll_down
    scroll_down
    row_0_data = ["00:00:00", "1", "1", "2", "1", "1", "Fake Location"] #midnight
    row_1_data = ["10:15:00", "5", "1", "1", "", "1", ""] #login
    row_2_data = ["10:15:00", "1", "4", "3", "1", "1", "Fake Location"] #on duty
    row_3_data = ["13:15:00", "5", "2", "4", "", "1", ""] #logout
    row_4_data = ["13:15:00", "1", "1", "5", "1", "1", "Fake Location"] #off duty


    compare = [row_0_data, row_1_data, row_2_data, row_3_data, row_4_data]
    rows = read_rows_from_event_table

    compare_rsi_ui_event_table_data(compare, rows)
end

def compare_rsi_ui_event_table_data(compare, rows)
    # define array of field names. These names are ordered by column index. Cannot have gaps!
    field_names = ["EventTime", "EventType", "EventCode", "SequenceIdNumber", "RecordStatus", "Origin", "Location"]
   
    # set failure indicator to false
    failExists = false

    # create blank array to hold error messages
    failMessages = []

    # ensure the number of rows match. If not, indicate a failure
    unless rows.length == compare.length
        failExists = true
        failMessages.push("Number of records do not match. Expected: #{compare.length}, Actual: #{rows.length}")
        puts "***Compare***".blue
        print_table_to_console(compare)

        puts "***Actual***".red
        print_table_to_console(rows)

    end

    # run through each of the gathered rows
    for i in 0..(rows.length - 1)
        # ensure we have an expected row to compare to
        if (i > (compare.length - 1))
            failExists = true
            failMessages.push("Unexpected row!")
            print_table_to_console(rows)
            scroll_down
            break
        end

        # check each field in the list of specified fields to check
        for j in 0..(field_names.length - 1)
            # define "actual" value from UI
            actual = rows[i][j]

            # define "expected" value
            expected = compare[i][j]
            
            # compare expected to actual
            unless expected == actual
                # if they don't match, set failure indicator to True
                failExists = true

                # add a failure message
                failMessages.push("[#{i}:#{j}] - #{field_names[j]} does not match. Expected: #{expected}, Actual: #{actual}")
            end
        end
    end
    if failExists == true
        errorMsg = ""
        failMessages.each do |msg|
            errorMsg += msg + "\n"
        end
        fail errorMsg
    end
end

def print_table_to_console(table)
    for i in 0..(table.length - 1)
        for j in 0..(table[i].length - 1)
            puts "#{table[i][j]}\n"
        end
        puts "***Next Row***"
    end
end

Then(/^I perform a Data Transfer via Web Services with comment "(.*?)" and certify logs$/) do |comment|
    #Prerequisite: RODS is displayed
    macro 'I click the Roadside Inspection button'
    macro 'I see options for Data Transfer and Roadside Inspection Mode'
    touch("Button text:'Data Transfer'")
	#wait for popup message, then choose Yes
	wait_for(60) {element_exists("TextView text:'Driver confirmation to initiate data transfer?'")}
	touch("Button text:'Yes'")
	#wait for popup message, then choose Yes
	wait_for(60) {element_exists("* {text CONTAINS 'Certify/Recertify Logs'}")}
	touch("Button text:'Yes'")
	#Certify Logs screen appears
	macro 'I should see Certify Logs screen'
	touch("CheckBox id:'chkLogDate'")
	touch("button marked:'Submit'")
	wait_for(30) {element_exists("button marked:'Agree'")}
	touch("button marked:'Agree'")
	#Roadside Data Transfer Method screen appears
	wait_for(30) {element_exists("TextView text:'Roadside Data Transfer Method'")}
	#choose method "Transfer Output File via Web Services"
    choose_data_transfer_method("web services")
    #enter Output File Comment
    touch("EditText id:'output_file_comment_text'")
    keyboard_enter_text comment
	hide_soft_keyboard
    #choose Transfer
    touch("Button text:'Transfer'")
    #wait for success message, click OK
   	wait_for(60) {element_exists("TextView text:'File transfer completed successfully'")}
	touch("Button text:'OK'")
	#wait for RODS
	macro 'I should see RODS'
end

def choose_data_transfer_method(option)
	#pass in "email" or "web services"
	if(option == "email")
		#choose email
		touch("RadioButton text:'Email Output File'")
	end
	if(option == "web services")
		#choose web services
		touch("RadioButton text:'Transfer Output File via Web Services'")
	end
end