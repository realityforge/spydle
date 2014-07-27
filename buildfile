require 'buildr/git_auto_version'

desc 'Spydle: Lightweight Monitoring Software'
define 'spydle' do
  project.group = 'org.realityforge'
  compile.options.source = '1.7'
  compile.options.target = '1.7'
  compile.options.lint = 'all'

  compile.with :javax_jsr305,
               :joda_time,
               :getopt4j,
               :javax_json

  package(:jar)
  package(:jar, :classifier => 'all').tap do |pkg|
    pkg.merge(artifacts([:getopt4j,:javax_json]))
    pkg.with :manifest => manifest.merge( 'Main-Class' => 'org.realityforge.spydle.Main' )
  end

  test.using :testng
  test.compile.with :mockito

  ipr.add_java_configuration(project,
                             'org.realityforge.spydle.Main',
                             :name => 'Spydle',
                             :dir => 'file://$PROJECT_DIR$/',
                             :args => '-d conf.d')
end
