try:
    # Try using ez_setup to install setuptools if not already installed.
    from ez_setup import use_setuptools
    use_setuptools()
except ImportError:
    # Ignore import error and assume Python 3 which already has setuptools.
    pass

from setuptools import setup, find_packages

classifiers = ['Development Status :: Ongoing',
               'Operating System :: POSIX :: Linux',
               'License :: OSI Approved :: MIT License',
               'Intended Audience :: Developers',
               'Programming Language :: Python :: 3',
               'Topic :: Software Development',
               'Topic :: System :: Hardware']

setup(name              = 'IRobotCreate',
      version           = '2.0',
      author            = 'Chris Cantrell',
      author_email      = 'topherCantrell@gmail.com',
      description       = 'Python code to control the irobotCreate2 from a raspberry pi',
      license           = 'MIT',
      classifiers       = classifiers,
      url               = 'https://github.com/topherCantrell/robots-piCreate',      
      packages          = find_packages())