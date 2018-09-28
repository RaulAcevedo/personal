require 'calabash-android/management/adb'
require 'calabash-android/operations'

Before do |scenario|
	start_test_server_in_background
	#  system 'adb logcat -c'
end

After do |scenario|
  if scenario.failed?
    screenshot_embed
#    puts adblog = 'adb logcat -v time -d | grep "AndroidRuntime"'
  end
  shutdown_test_server
end
