require 'buildr/git_auto_version'

download(artifact(:getopt4j) => 'http://cloud.github.com/downloads/realityforge/getopt4j/getopt4j-1.0.jar')
download(artifact(:getopt4j_sources) => 'http://cloud.github.com/downloads/realityforge/getopt4j/getopt4j-1.0-sources.jar')

desc 'Spydle: Lightweight Monitoring Software'
define('spydle') do
  project.group = 'org.realityforge'
  compile.options.source = '1.7'
  compile.options.target = '1.7'
  compile.options.lint = 'all'

  compile.with :javax_annotation,
               :joda_time,
               :getopt4j,
               :json_simple

  package(:jar)
  package(:jar, :classifier => 'all').tap do |pkg|
    pkg.merge(artifacts([:getopt4j,:json_simple]))
    pkg.with :manifest => manifest.merge( 'Main-Class' => 'org.realityforge.spydle.Main' )
  end

  test.using :testng
  test.compile.with :mockito
end
