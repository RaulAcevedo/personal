Then(/^KMB is closed$/) do
	wait_for(180) {element_does_not_exist("button marked:'Submit'")}
end

