# BLE Controller

## Requirements
- `sudo apt install libbluetooth-dev`
- `sudo pip3 install pybluez`

Gattlib requires an additional glib2.0 package, therefore install:
- `sudo apt install libperl-dev`
- `sudo apt install libgtk2.0-dev`

or
- `sudo apt install libglib2.0-dev`

Now we can install gattlib:
- `sudo apt install libboost-thread-dev`
- `sudo pip3 install libboost-python-dev`

- `pip3 download gattlib`
- `tar xvzf ./gattlib-0.20150805.tar.gz`
-` cd gattlib-0.20150805/`
- `sed -ie 's/boost_python-py34/boost_python-py35/' setup.py`
- `pip3 install .`

- `sudo pip3 install bluepy`

