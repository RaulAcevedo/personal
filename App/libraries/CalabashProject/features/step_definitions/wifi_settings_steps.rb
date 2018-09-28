Given(/^I see a checkbox for Enable WiFi$/) do
    chkbx = query("CheckBox id:'is_enabled'")
    if(chkbx.empty?)
        fail("Enable WiFi checkbox is not present!")
    end
end

Given(/^I see WiFi is enabled$/) do
    actual_text = query("TextView id:'enabled_status'", :text).first
    expected_text = "(WiFi is currently enabled)"
    unless actual_text == expected_text
        fail("WiFi enabled message is not appearing as expected. Expected: #{expected_text}, Actual: #{actual_text}")
    end
end