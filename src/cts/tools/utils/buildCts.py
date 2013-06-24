#!/usr/bin/python

# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Module for generating CTS test descriptions and test plans."""

import glob
import os
import re
import subprocess
import sys
import xml.dom.minidom as dom
from cts import tools
from multiprocessing import Pool

def GetSubDirectories(root):
  """Return all directories under the given root directory."""
  return [x for x in os.listdir(root) if os.path.isdir(os.path.join(root, x))]


def GetMakeFileVars(makefile_path):
  """Extracts variable definitions from the given make file.

  Args:
    makefile_path: Path to the make file.

  Returns:
    A dictionary mapping variable names to their assigned value.
  """
  result = {}
  pattern = re.compile(r'^\s*([^:#=\s]+)\s*:=\s*(.*?[^\\])$', re.MULTILINE + re.DOTALL)
  stream = open(makefile_path, 'r')
  content = stream.read()
  for match in pattern.finditer(content):
    result[match.group(1)] = match.group(2)
  stream.close()
  return result


class CtsBuilder(object):
  """Main class for generating test descriptions and test plans."""

  def __init__(self, argv):
    """Initialize the CtsBuilder from command line arguments."""
    if not len(argv) == 6:
      print 'Usage: %s <testRoot> <ctsOutputDir> <tempDir> <androidRootDir> <docletPath>' % argv[0]
      print ''
      print 'testRoot:       Directory under which to search for CTS tests.'
      print 'ctsOutputDir:   Directory in which the CTS repository should be created.'
      print 'tempDir:        Directory to use for storing temporary files.'
      print 'androidRootDir: Root directory of the Android source tree.'
      print 'docletPath:     Class path where the DescriptionGenerator doclet can be found.'
      sys.exit(1)
    self.test_root = sys.argv[1]
    self.out_dir = sys.argv[2]
    self.temp_dir = sys.argv[3]
    self.android_root = sys.argv[4]
    self.doclet_path = sys.argv[5]

    self.test_repository = os.path.join(self.out_dir, 'repository/testcases')
    self.plan_repository = os.path.join(self.out_dir, 'repository/plans')

  def GenerateTestDescriptions(self):
    """Generate test descriptions for all packages."""
    pool = Pool(processes=16)

    # individually generate descriptions not following conventions
    pool.apply_async(GenerateSignatureCheckDescription, [self.test_repository])
    pool.apply_async(GenerateReferenceAppDescription, [self.test_repository])
    pool.apply_async(GenerateAppSecurityDescription, [self.temp_dir,
        self.test_repository, self.android_root, self.doclet_path])

    # generate test descriptions for android tests
    android_packages = GetSubDirectories(self.test_root)
    for package in android_packages:
      pool.apply_async(GenerateTestDescription, [self.test_root, self.temp_dir,
          self.test_repository, self.android_root, self.doclet_path, package])

    pool.close()
    pool.join()

  def __WritePlan(self, plan, plan_name):
    print 'Generating test plan %s' % plan_name
    plan.Write(os.path.join(self.plan_repository, plan_name + '.xml'))

  def GenerateTestPlans(self):
    """Generate default test plans."""
    # TODO: Instead of hard-coding the plans here, use a configuration file,
    # such as test_defs.xml
    packages = []
    descriptions = sorted(glob.glob(os.path.join(self.test_repository, '*.xml')))
    for description in descriptions:
      doc = tools.XmlFile(description)
      packages.append(doc.GetAttr('TestPackage', 'appPackageName'))

    plan = tools.TestPlan(packages)
    plan.Exclude('android\.performance.*')
    self.__WritePlan(plan, 'CTS')
    plan.Exclude(r'android\.tests\.sigtest')
    plan.Exclude(r'android\.core.*')
    self.__WritePlan(plan, 'Android')

    plan = tools.TestPlan(packages)
    plan.Include(r'android\.core\.tests.*')
    self.__WritePlan(plan, 'Java')

    plan = tools.TestPlan(packages)
    plan.Include(r'android\.core\.vm-tests')
    self.__WritePlan(plan, 'VM')

    plan = tools.TestPlan(packages)
    plan.Include(r'android\.tests\.sigtest')
    self.__WritePlan(plan, 'Signature')

    plan = tools.TestPlan(packages)
    plan.Include(r'android\.apidemos\.cts')
    self.__WritePlan(plan, 'RefApp')

    plan = tools.TestPlan(packages)
    plan.Include(r'android\.performance.*')
    self.__WritePlan(plan, 'Performance')

    plan = tools.TestPlan(packages)
    plan.Include(r'android\.tests\.appsecurity')
    self.__WritePlan(plan, 'AppSecurity')

def LogGenerateDescription(name):
  print 'Generating test description for package %s' % name

def GenerateSignatureCheckDescription(test_repository):
  """Generate the test description for the signature check."""
  LogGenerateDescription('android.tests.sigtest')
  package = tools.TestPackage('SignatureTest', 'android.tests.sigtest')
  package.AddAttribute('appNameSpace', 'android.tests.sigtest')
  package.AddAttribute('signatureCheck', 'true')
  package.AddAttribute('runner', '.InstrumentationRunner')
  package.AddTest('android.tests.sigtest.SignatureTest.signatureTest')
  description = open(os.path.join(test_repository, 'SignatureTest.xml'), 'w')
  package.WriteDescription(description)
  description.close()

def GenerateReferenceAppDescription(test_repository):
  """Generate the test description for the reference app tests."""
  LogGenerateDescription('android.apidemos.cts')
  package = tools.TestPackage('ApiDemosReferenceTest', 'android.apidemos.cts')
  package.AddAttribute('appNameSpace', 'android.apidemos.cts')
  package.AddAttribute('packageToTest', 'com.example.android.apis')
  package.AddAttribute('apkToTestName', 'ApiDemos')
  package.AddAttribute('runner', 'android.test.InstrumentationTestRunner')
  package.AddAttribute('referenceAppTest', 'true')
  package.AddTest('android.apidemos.cts.ApiDemosTest.testNumberOfItemsInListView')
  description = open(os.path.join(test_repository, 'ApiDemosReferenceTest.xml'), 'w')
  package.WriteDescription(description)
  description.close()

def GenerateAppSecurityDescription(temp_dir, test_repository, android_root, doclet_path):
  """Generate the test description for the application security tests."""
  test_root = 'cts/tests/appsecurity-tests'
  makefile_name = os.path.join(test_root, 'Android.mk')
  makefile_vars = GetMakeFileVars(makefile_name)
  name = makefile_vars['LOCAL_MODULE']
  package_name = 'android.tests.appsecurity'
  LogGenerateDescription(package_name)
  temp_desc = os.path.join(temp_dir, 'description.xml')
  RunDescriptionGeneratorDoclet(android_root, doclet_path,
      os.path.join(test_root, 'src'), temp_desc)
  doc = dom.parse(temp_desc)
  test_description = doc.getElementsByTagName('TestPackage')[0]
  test_description.setAttribute('name', package_name)
  test_description.setAttribute('appPackageName', package_name)
  test_description.setAttribute('hostSideOnly', 'true')
  test_description.setAttribute('jarPath', name + '.jar')
  description = open(os.path.join(test_repository, package_name + '.xml'), 'w')
  doc.writexml(description, addindent='    ', encoding='UTF-8')
  description.close()


def GenerateTestDescription(test_root, temp_dir, test_repository, android_root,
                            doclet_path, package):

  app_package_name = 'android.' + package
  package_root = os.path.join(test_root, package)

  makefile_name = os.path.join(package_root, 'Android.mk')
  if not os.path.exists(makefile_name):
    print 'Skipping directory "%s" due to missing Android.mk' % package_root
    return
  makefile_vars = GetMakeFileVars(makefile_name)

  manifest_name = os.path.join(package_root, 'AndroidManifest.xml')
  if not os.path.exists(manifest_name):
    print 'Skipping directory "%s" due to missing AndroidManifest.xml' % package_root
    return
  manifest = tools.XmlFile(manifest_name)

  LogGenerateDescription(app_package_name)

  # Run the description generator doclet to get the test package structure
  # TODO: The Doclet does not currently add all required attributes. Instead of rewriting
  # the document below, additional attributes should be passed to the Doclet as arguments.
  temp_desc = os.path.join(temp_dir, app_package_name + '-description.xml')

  RunDescriptionGeneratorDoclet(android_root, doclet_path, package_root, temp_desc)

  # obtain missing attribute values from the makefile and manifest
  package_name = makefile_vars['LOCAL_PACKAGE_NAME']
  runner = manifest.GetAndroidAttr('instrumentation', 'name')
  target_package = manifest.GetAndroidAttr('instrumentation', 'targetPackage')
  target_binary_name = makefile_vars.get('LOCAL_INSTRUMENTATION_FOR')

  # add them to the document
  doc = dom.parse(temp_desc)
  test_description = doc.getElementsByTagName('TestPackage')[0]
  test_description.setAttribute('name', package_name)
  test_description.setAttribute('runner', runner)
  test_package = manifest.GetAttr('manifest', 'package')
  test_description.setAttribute('appNameSpace', test_package)
  test_description.setAttribute('appPackageName', app_package_name)
  if not test_package == target_package:
    test_description.setAttribute('targetNameSpace', target_package)
    test_description.setAttribute('targetBinaryName', target_binary_name)
  description = open(os.path.join(test_repository, package_name + '.xml'), 'w')
  doc.writexml(description, addindent='    ', encoding='UTF-8')
  description.close()

def RunDescriptionGeneratorDoclet(android_root, doclet_path, source_root, output_file):
  """Generate a test package description by running the DescriptionGenerator doclet.

  Args:
    android_root: Root directory of the Android source tree.
    doclet_path: Class path where the DescriptionGenerator doclet can be found.
    source_root: Directory under which tests should be searched.
    output_file: Name of the file where the description gets written.

  Returns:
    The exit code of the DescriptionGenerator doclet run.
  """
  # Make sure sourceRoot is relative to  self.android_root
  source_root = RelPath(source_root, android_root)

  # To determine whether a class is a JUnit test, the Doclet needs to have all intermediate
  # subclasses of TestCase as well as the JUnit framework itself on the source path.
  # Annotation classes are also required, since test annotations go into the description.
  source_path = [
      'frameworks/base/core/java',            # android test classes
      'frameworks/base/test-runner/src',      # test runner
      'libcore/junit/src/main/java',          # junit classes
      'development/tools/hosttestlib/src',    # hosttestlib TestCase extensions
      'libcore/dalvik/src/main/java',         # test annotations
      'cts/tests/src',                        # cts test stubs
      source_root                             # the source for this package
  ]
  source_path = [os.path.join(android_root, x) for x in source_path]
  cmd = ('javadoc -o %s -J-Xmx512m -quiet -doclet DescriptionGenerator -docletpath %s'
         ' -sourcepath %s ') % (output_file, doclet_path, ':'.join(source_path))
  sources = []

  def AddFile(sources, folder, names):
    """Find *.java."""
    sources.extend([os.path.join(folder, name) for name in names if name.endswith('.java')])

  os.path.walk(os.path.join(android_root, source_root), AddFile, sources)
  cmd += ' '.join(sources)
  proc = subprocess.Popen(cmd, shell=True, stderr=subprocess.STDOUT, stdout=subprocess.PIPE)
  # read and discard any output
  proc.communicate()
  # wait for process to terminate and return exit value
  return proc.wait()

def RelPath(path, start=os.getcwd()):
  """Get a relative version of a path.

  This is equivalent to os.path.relpath, which is only available since Python 2.6.

  Args:
    path: The path to transform.
    start: The base path. Defaults to the current working directory.

  Returns:
    A transformed path that is relative to start.
  """
  path_dirs = os.path.abspath(path).split(os.path.sep)
  start_dirs = os.path.abspath(start).split(os.path.sep)

  num_common = len(os.path.commonprefix([start_dirs, path_dirs]))

  result_dirs = ['..'] * (len(start_dirs) - num_common) + path_dirs[num_common:]
  if result_dirs:
    return os.path.join(*result_dirs)
  return start

if __name__ == '__main__':
  builder = CtsBuilder(sys.argv)
  builder.GenerateTestDescriptions()
  builder.GenerateTestPlans()
