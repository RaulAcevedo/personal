Then (/^I see Trailer Number "(.*?)" and Shipment Info "(.*?)"$/) do |trailer, shipment|
	trail = query("TextView id:'txtTrailertripinfo'",:text).first
	unless trail == trailer
		fail "Trailer Number incorrect.  Shows '#{trail}'."
	end
	ship = query("TextView id:'txtShipmentInfotripinfo'",:text).first
	unless ship == shipment
		fail "Shipment Info incorrect.  Shows '#{ship}'."
	end
end

