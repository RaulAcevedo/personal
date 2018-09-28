require 'calabash-android/management/app_installation'

#updated when I updated to Calabash 0.9.0
#AfterConfiguration do |config|
#	FeatureNameMemory.feature_name = nil
#end

#Before do |scenario|
#  @scenario_is_outline = (scenario.class == Cucumber::Ast::OutlineTable::ExampleRow)
#  if @scenario_is_outline 
#    scenario = scenario.scenario_outline 
#  end 

#  feature_name = scenario.feature.title
#  if FeatureNameMemory.feature_name != feature_name \
#      or ENV["RESET_BETWEEN_SCENARIOS"] == "1"
#    if ENV["RESET_BETWEEN_SCENARIOS"] == "1"
#      log "New scenario - reinstalling apps"
#    else
#      log "First scenario in feature - reinstalling apps"
#    end
    
#    uninstall_apps
#    install_app(ENV["TEST_APP_PATH"])
#    install_app(ENV["APP_PATH"])
#    FeatureNameMemory.feature_name = feature_name
#	FeatureNameMemory.invocation = 1
#  else
#    FeatureNameMemory.invocation += 1
#  end
#end

#FeatureNameMemory = Class.new
#class << FeatureNameMemory
#  @feature_name = nil
#  attr_accessor :feature_name, :invocation
#end

AfterConfiguration do |config|
  FeatureMemory.feature = nil
end

Before do |scenario|
  scenario = scenario.scenario_outline if scenario.respond_to?(:scenario_outline)

  feature = scenario.feature
  if FeatureMemory.feature != feature || ENV['RESET_BETWEEN_SCENARIOS'] == '1'
    if ENV['RESET_BETWEEN_SCENARIOS'] == '1'
      log 'New scenario - reinstalling apps'
    else
      log 'First scenario in feature - reinstalling apps'
    end

    ensure_app_installed
    clear_app_data
    FeatureMemory.feature = feature
    FeatureMemory.invocation = 1
  else
    FeatureMemory.invocation += 1
  end
end

FeatureMemory = Struct.new(:feature, :invocation).new