require 'buildr/java/emma'

desc "Spydle: Lightweight Monitoring Software"
define('spydle') do
  project.version = `git describe --tags`.strip
  project.group = 'org.realityforge'
  compile.options.source = '1.6'
  compile.options.target = '1.6'
  compile.options.lint = 'all'

  compile.with :javax_annotation,
               :hsqldb

  package(:jar)
  package(:sources)

  test.using :testng
  test.compile.with :mockito

  emma.include 'org.realityforge.*'

  ipr.extra_modules << '../jcollectd/jcollectd.iml'
  ipr.extra_modules << '../membrane/membrane.iml'
  ipr.extra_modules << '../jrds/jrds.iml'
  ipr.extra_modules << '../gdash/gdash.iml'
  ipr.extra_modules << '../graphite-relay/graphite-relay.iml'
end
