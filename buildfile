require 'buildr/java/emma'

desc "Spydle: Lightweight Monitoring Software"
define('spydle') do
  project.version = `git describe --tags`.strip
  project.group = 'org.realityforge'
  compile.options.source = '1.6'
  compile.options.target = '1.6'
  compile.options.lint = 'all'

  package(:jar)
  package(:sources)

  test.using :testng
  test.compile.with :mockito

  emma.include 'org.realityforge.*'
end
