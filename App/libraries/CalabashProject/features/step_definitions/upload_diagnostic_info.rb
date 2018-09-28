When(/^I upload diagnostics$/) do
	touch("button marked:'Upload'")
	wait_for(45) {element_exists("Button marked:'OK'")}
	touch("button marked:'OK'")
	sleep(2)
end