Given(/^I am asked who will be the first to drive$/) do
	wait_for(30) {element_exists("TextView text:'Who will be the first person to drive the vehicle?'")}
end

Then(/^I choose "(.*?)"$/) do |lastfirst|
	touch("button marked:'#{lastfirst}'")
end	
