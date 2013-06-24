Android Audio Quality Verifier App
==================================

This app runs a set of audio quality tests on an Android device,
to verify the end-end sound recording path.

If any of these tests fail, the device is probably unsuitable
for demanding audio tasks such as speech recognition.
If all the tests pass, the device audio is of a good standard.
Note that not all possible audio defects can be detected by this
test suite, so passing does not guarantee ideal audio quality.

Hardware setup
--------------

The required physical set-up consists of a powered speaker,
connected to the Android's headphone output by a standard
audio cable.

For loudspeakers which come in pairs, you only need to use
one speaker (typically the powered or master speaker); you
can leave the second speaker disconnected.
If the speakers are stereo within a single unit (sometimes
with speakers facing in opposite directions), place the phone
in front of either of them.
Speakers with multiple drivers per channel (e.g. a tweeter
and a woofer) are not suitable.

The phone should be placed in front of the centre of the
speaker cone. The distance from the speaker will be adjusted
during calibration; typically you could expect it to be around
3cm or so.
Use a supporting platform such as a stack of books to raise
the phone to the correct height to line up with the speaker.

Bluetooth connection is possible but cable connection is
usually preferable.

Recommended loudspeakers
------------------------

Using suitable loudspeakers ensures that test failures highlight
problems with the Android device under test, and not limitations
of the loudspeakers. The following loudspeakers work well for this
purpose:

1. Yamaha NX-B02

Use on AC power, not batteries.
This speaker works well with Bluetooth as well as a wired connection.
Note that it's not uncommon for the devices to exhibit different
bugs under Bluetooth.

2. Cakewalk MA-7A (Edirol / Roland)

The "Bass Enhancer" feature MUST be switched off.
Note that it turns itself on again every time the speakers are
powered on, so it is easy to forget to switch it off!

Software setup
--------------

1. Build the application's apk.
2. Install the apk using adb.
3. Run the app.
4. Click "Calibrate". Position the phone as described in
   Hardware setup above, with the microphone facing the speaker,
   and adjust the volume of the speaker until the status message
   indicates it is correct.
5. Click on any test in the list to run it, or "Run All" to run
   each test in sequence.
6. Click "Results" to view the outcomes. A correctly functioning
   device should pass all tests.
7. Click "Send by email" from the results page to send the
   results to an e-mail address of your choice. The recordings
   made are also attached as raw 16 bit, 16 kHz audio files to
   help you diagnose any failed tests.

Q&A
---

Q. What if the sound level check fails?
A. Go back to the calibration step before running any other test.
   Make sure the device has not been moved.
   We also recommend that once the setup is calibrated there are no
   moving objects or people near the device under test, since these
   will change the acoustic properties of the environment from the
   calibrated state.

Q. Some of the tests sound very loud. Is this normal?
A. The clipping test will generally be very loud indeed;
   the others should be at a moderate volume.

Q. What sort of room should the tests be performed in?
A. Any, as long as the background noise levels are kept low, to
   avoid interference with the test recordings.
