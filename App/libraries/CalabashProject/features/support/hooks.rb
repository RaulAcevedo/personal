After('@ComplianceTablet') do |scenario|
    if scenario.failed?
        print "turning off engine because the test failed"
        macro 'I turn off engine'
    end
end