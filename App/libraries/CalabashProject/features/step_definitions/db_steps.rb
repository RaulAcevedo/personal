When(/^I get version of a new SQLite db$/) do
    #!/usr/bin/ruby

    require 'sqlite3'

    begin
        
        db = SQLite3::Database.new ":memory:"
        puts db.get_first_value 'SELECT SQLITE_VERSION()'
        
    rescue SQLite3::Exception => e 
        
        puts "Exception occurred"
        puts e
        
    ensure
        db.close if db
    end
end

# The implementation below requires the sqlite3 command line interface be available at the following location:
# This also requires .txt files to be created with the exact queries necessary and placed in the proper location
# How this works:
#   1) using the CLI for sqlite3, open the DB file
#   2) Execute the specified query saved in the Scripts folder and save the results in the output folder as a .csv file
#   3) Build a comparison .csv file in memory based on the data you expect to compare
#   4) Read the saved .csv file from step 2, and compare it, line by line, to the .csv file in memory created in step 3
#   5) Print out any differences, and log a failure at the end if anything was not correct.

When(/^I run the db script "(.*)" against the db named "(.*)"$/) do |scriptFileName, dbFileName|
    system "cd #{ENV['DB_FILE_PATH']}; sqlite3/sqlite3 #{dbFileName} \".read Scripts/#{scriptFileName}\""
end

When(/^I pull the db from my device as "(.*)"$/) do |fileName|
    # This pulls the kmb database file to the dbFiles folder
    #system "adb pull storage/self/primary/kmbdiag/kmb #{ENV['DB_FILE_PATH']}"
    system "adb pull sdcard/kmbdiag/kmb #{ENV['DB_FILE_PATH']}"
    # rename the file
    system "mv #{ENV['DB_FILE_PATH']}kmb #{ENV['DB_FILE_PATH']}#{fileName}"
end

When(/^I read the csv file "(.*)"$/) do |fileName|
    rows = CSV.read("#{ENV['DB_FILE_PATH']}output/#{fileName}")
end

Given(/^I see the correct events for my Edit ELD Events scenario$/) do
    # create comparison values
    compareRaw = CSV.generate(write_headers:true) do |csv|
        csv << ["EventRecordStatus", "EventRecordOrigin", "EventType", "EventCode", "EventDateTime"]
        csv << ["1", "1", "1", "1", "Yesterday - Midnight"]
        csv << ["1", "1", "1", "1", "Yesterday - Midnight"]
        csv << ["1", "1", "1", "4", "Yesterday - 8AM"]
        csv << ["1", "1", "1", "4", "Yesterday - 8AM"]
        csv << ["1", "1", "1", "3", "Yesterday - 9AM"]
        csv << ["1", "1", "1", "3", "Yesterday - 9AM"]
        csv << ["1", "1", "1", "4", "Yesterday - 11AM"]
        csv << ["2", "1", "1", "4", "Yesterday - 11AM"]
        csv << ["1", "2", "1", "3", "Yesterday - 11AM"]
        csv << ["1", "2", "1", "4", "Yesterday - 11:15AM"]
        csv << ["1", "1", "1", "1", "Yesterday - 1PM"]
        csv << ["1", "1", "1", "1", "Yesterday - 1PM"]
        csv << ["1", "2", "1", "1", "Today - Midnight"]
        csv << ["1", "2", "1", "4", "Today - Login"]
        csv << ["1", "1", "5", "1", "Today - Login"]
        csv << ["2", "2", "1", "2", "Today - Login"]
    end

    # build the .csv file in memory from the csv array declared above.
    compare = CSV.parse(compareRaw, headers:true)

    # Identify which fields need to be checked in the results and save them in an array
    #fields_to_check = ["EventRecordStatus", "EventRecordOrigin", "EventType", "EventCode", "EventDateTime"]
    fields_to_check = ["EventRecordStatus", "EventRecordOrigin", "EventType", "EventCode"]

    # read the .csv file to a variable
    rows = CSV.read("#{ENV['DB_FILE_PATH']}output/eldEvent_Regression.csv", headers:true)

    # declare a variable to track whether or not something went wrong. We do this so we do the entire comparison before failing
    isFailure = false

    # check that the number of rows match
    unless rows.length == compare.length
        isFailure = true
        eLength = compare.length
        cLength = rows.length
        print "\nNumber of records in database (#{cLength}) does not match expected (#{eLength})\n"
    end

    # for each row, verify the data
    for i in 0..(rows.length - 1)
        # for each field to check, verify the field matches
        fields_to_check.each do |fieldName|
            actual = rows[i]["#{fieldName}"]
            expected = compare[i]["#{fieldName}"]

            # if the fields don't match, print out an error message indicating the 2 values, and set the failure flag to TRUE
            unless (actual == expected)
                isFailure = true
                print "\nfailure when checking the Database. Field \"#{fieldName}\" - expected: \"#{expected}\", actual \"#{actual}\"\n"
            end
        end
    end

    # when it's all complete, if there was a failure, fail the step
    unless isFailure == false
        fail "\nDatabases did not match!\n"
    end
end
