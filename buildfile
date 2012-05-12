require 'buildr/java/emma'

desc "Spydle: Lightweight Monitoring Software"
define('spydle') do
  project.version = `git describe --tags --always`.strip
  project.group = 'org.realityforge'
  compile.options.source = '1.7'
  compile.options.target = '1.7'
  compile.options.lint = 'all'

  compile.with :javax_annotation,
               :spice_cli,
               :json_simple,
               :hsqldb

  package(:jar)
  package(:sources)

  test.using :testng
  test.compile.with :mockito

  emma.include 'org.realityforge.*'

  ipr.extra_modules << '../gdash/gdash.iml'
  ipr.extra_modules << '../ruby-statsd/ruby-statsd.iml'
  ipr.extra_modules << '../statsd.scala/statsd.scala.iml'
end
