Then(/^I select Driver 2 Login Button$/) do
	wait_for(60) {element_exists("Button marked:'Driver #2 Login'")}
	touch("Button marked:'Driver #2 Login'")
end
