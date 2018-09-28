Given(/^Tractor Number on Vehicle Inspection is the connected ELD$/) do
  expected_property = ENV['UNIT_ID']
  actual_property = query("TextView id:'text1'", :text).first
  unless expected_property == actual_property
    fail "Tractor Number is incorrect.  Expected it to show '#{ENV['UNIT_ID']}' but was '#{actual_property}'"
  end
end

Given(/^No Defects checkbox is checked$/) do
  actual_property = query("CheckBox id:'chkNoDefects'", :checked).first
  unless actual_property
    fail "No Defects checkbox is not checked."
  end
end
