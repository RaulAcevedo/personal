Then(/^I should see a Log Remark "(.*?)"$/) do |remark|
	actual_remark = query("TextView id:'tvRemark'",:text).first
	counter = 0
	while actual_remark.nil?
		break if counter == 30
		scroll("GridView", :down)
		actual_remark = query("TextView id:'tvRemark'",:text).first
		counter = counter + 1
	end
	if counter == 30
		fail("Log Remark for '#{remark}' could not be found.")
	else
		unless actual_remark.include? remark
			fail "Log Remark for '#{remark}' is not displayed.  Actual Remark found was '#{actual_remark}'."
		end
	end
end





