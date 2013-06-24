#include <stdio.h>
#include <unistd.h>
#include <EGL/egl.h>
#include <GLES/gl.h>
#include <pthread.h>
#include <ui/EventHub.h>
#include <ui/FramebufferNativeWindow.h>
#include <ui/EGLUtils.h>

extern void AndroidInitArgs(int argc, char** argv);
extern int AndroidInit();
extern int AndroidMotionEvent(unsigned long long eventTime, int action,
        float x, float y, float pressure, float size, int deviceId);
extern int AndroidEvent(int type, int value);
extern int AndroidStep(int width, int height);

static int gDisplayWidth;
static int gDisplayHeight;
static EGLDisplay gDisplay;
static EGLSurface gSurface;
static EGLContext gContext;

void checkEGLError(const char* msg) {
    unsigned int error = eglGetError();
    if (error != EGL_SUCCESS) {
        fprintf(stderr, "%s: error %u\n", msg, error);
    }
}

void checkGLError(const char* msg) {
    unsigned int error = glGetError();
    if (error != GL_NO_ERROR) {
        fprintf(stderr, "%s: error 0x%04X\n", msg, error);
    }
}

static android::sp<android::EventHub> gHub;

class EventQueue {
private:
    class Lock {
    public:
        Lock(pthread_mutex_t& mutex) {
            m_pMutex = &mutex;
            pthread_mutex_lock(m_pMutex);
        }
        ~Lock() {
            pthread_mutex_unlock(m_pMutex);
        }
        void wait(pthread_cond_t& cond) {
            pthread_cond_wait(&cond, m_pMutex);
        }
        void signal(pthread_cond_t& cond) {
            pthread_cond_signal(&cond);
        }
    private:
        pthread_mutex_t* m_pMutex;
    };
    
public:
    
    static const int MOTION_ACTION_DOWN = 0;
    static const int MOTION_ACTION_UP = 1;
    static const int MOTION_ACTION_MOVE = 2;

// Platform-specific event types.

    static const int EV_DEVICE_ADDED = android::EventHub::DEVICE_ADDED;
    static const int EV_DEVICE_REMOVED = android::EventHub::DEVICE_REMOVED;
    
    struct Event {
        int32_t deviceId;
        int32_t type;
        int32_t scancode;
        int32_t keycode;
        uint32_t flags;
        int32_t value;
        nsecs_t when;
    };
    
    EventQueue() {
        m_Head = 0;
        m_Count = 0;
        pthread_mutex_init(&m_mutex, NULL);
        pthread_cond_init(&m_space_available, NULL);
        startEventThread();
    }
    
    // Returns NULL if no event available.
    // Call recycleEvent when you're done with the event
    Event* getEvent() {
        Event* result = NULL;
        Lock lock(m_mutex);
        if (m_Count > 0) {
            result = m_Events + m_Head;
        }
        return result;
    }
    
    void recycleEvent(Event* pEvent) {
        Lock lock(m_mutex);
        if (pEvent == m_Events + m_Head && m_Count > 0) {
            m_Head = incQueue(m_Head);
            m_Count--;
            lock.signal(m_space_available);
        }
    }
    
private:
    inline size_t incQueue(size_t index) {
        return modQueue(index + 1);
    }
    
    inline size_t modQueue(size_t index) {
        return index & EVENT_SIZE_MASK;
    }
    
    void startEventThread() {
        pthread_create( &m_eventThread, NULL, &staticEventThreadMain, this);
    }
    
    static void* staticEventThreadMain(void* arg) {
        return ((EventQueue*) arg)->eventThreadMain();
    }
    
    void* eventThreadMain() {
        gHub = new android::EventHub();
        while(true) {
            android::RawEvent rawEvent;
            bool result = gHub->getEvent(& rawEvent);
            if (result) {
                Event event;
                event.deviceId = rawEvent.deviceId;
                event.when = rawEvent.when;
                event.type = rawEvent.type;
                event.value = rawEvent.value;
                event.keycode = rawEvent.keyCode;
                event.scancode = rawEvent.scanCode;
                event.flags = rawEvent.flags;

                Lock lock(m_mutex);
                while( m_Count == MAX_EVENTS) {
                    lock.wait(m_space_available);
                }
                m_Events[modQueue(m_Head + m_Count)] = event;
                m_Count = incQueue(m_Count);
            }
        }
        return NULL;
    }

    static const size_t MAX_EVENTS = 16;
    static const size_t EVENT_SIZE_MASK = 0xf;
    
    pthread_t m_eventThread;
    pthread_mutex_t m_mutex;
    pthread_cond_t  m_space_available;
    unsigned int m_Head;
    unsigned int m_Count;
    Event m_Events[MAX_EVENTS];
};

bool gNoEvents;
EventQueue* gpEventQueue;

int init(int argc, char** argv) {
    
    for(int i = 0; i < argc; i++) {
        char* p = argv[i];
        if (strcmp(p, "-noevents") == 0) {
            printf("-noevents: will not look for events.\n");
            gNoEvents = true;
        }
    }
    
    if (! gNoEvents) {
        gpEventQueue = new EventQueue();
    }

    EGLNativeWindowType window = android_createDisplaySurface();

    gDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    
    EGLint majorVersion;
    EGLint minorVersion;
    
    eglInitialize(gDisplay, &majorVersion, &minorVersion);
    checkEGLError("eglInitialize");
        
    EGLint configRequest[] = {
            EGL_SURFACE_TYPE, EGL_PBUFFER_BIT|EGL_WINDOW_BIT,
            EGL_DEPTH_SIZE, 16,
            EGL_NONE
    };
 
    EGLConfig config;
    android::EGLUtils::selectConfigForNativeWindow(gDisplay, configRequest, window, &config);
    gSurface = eglCreateWindowSurface(gDisplay, config, window, NULL);

    eglQuerySurface(gDisplay, gSurface, EGL_WIDTH, &gDisplayWidth);
    eglQuerySurface(gDisplay, gSurface, EGL_HEIGHT, &gDisplayHeight);
    fprintf(stderr, "display width = %d, height = %d\n", gDisplayWidth,
            gDisplayHeight);

    gContext = eglCreateContext(gDisplay, config, NULL, NULL);
    checkEGLError("eglCreateContext");
    eglMakeCurrent(gDisplay, gSurface, gSurface, gContext);
    checkEGLError("eglMakeCurrent");
    
    printf("vendor    : %s\n", glGetString(GL_VENDOR));
    printf("renderer  : %s\n", glGetString(GL_RENDERER));
    printf("version   : %s\n", glGetString(GL_VERSION));
    printf("extensions: %s\n", glGetString(GL_EXTENSIONS));
        
    return 0;
}

// Quick and dirty implementation of absolute pointer events...

bool lastAbsDown = false;
bool absDown = false;
bool absChanged = false;
unsigned long long absDownTime = 0;
int absX = 0;
int absY = 0;
int absPressure = 0;
int absSize = 0;
int lastAbsX = 0;
int lastAbsY = 0;
int lastAbsPressure = 0;
int lastAbsSize = 0;


void checkEvents() {
    
    if (gpEventQueue == NULL) {
        return;
    }
    while(true) {
        EventQueue::Event* pEvent = gpEventQueue->getEvent();
        if (pEvent == NULL) {
            return;
        }
#if 1
        printf("Event deviceId: %d, type: %d, scancode: %d,  keyCode: %d, flags: %d, value: %d, when: %llu\n",
                pEvent->deviceId, pEvent->type, pEvent->scancode,
                pEvent->keycode, pEvent->flags, pEvent->value, pEvent->when);
#endif
        switch (pEvent->type) {
        case EV_KEY: // Keyboard input
            if (pEvent->scancode == BTN_TOUCH) {
                absDown = pEvent->value != 0;
                absChanged = true;
            }
            else {
                AndroidEvent(pEvent->value, pEvent->keycode);
            }
            break;
            
        case EV_ABS:
            if (pEvent->scancode == ABS_X) {
                absX = pEvent->value;
                absChanged = true;
            } else if (pEvent->scancode == ABS_Y) {
                absY = pEvent->value;
                absChanged = true;
            } else if (pEvent->scancode == ABS_PRESSURE) {
                absPressure = pEvent->value;
                absChanged = true;
            } else if (pEvent->scancode == ABS_TOOL_WIDTH) {
                absSize = pEvent->value;
                absChanged = true;
            }

        case EV_SYN:
        {
            if (absChanged) {
                 absChanged = false;
                 int action;
                 if (absDown != lastAbsDown) {
                     lastAbsDown = absDown;
                     if (absDown) {
                         action = EventQueue::MOTION_ACTION_DOWN;
                         absDownTime = pEvent->when;
                     } else {
                         action = EventQueue::MOTION_ACTION_UP;
                         absX = lastAbsX;
                         absY = lastAbsY;
                         absPressure = lastAbsPressure;
                         absSize = lastAbsSize;
                     }
                 } else {
                     action = EventQueue::MOTION_ACTION_MOVE;
                 }
                 float scaledX = absX;
                 float scaledY = absY;
                 float scaledPressure = 1.0f;
                 float scaledSize = 0;
#if 0
                 if (di != null) {
                     if (di.absX != null) {
                         scaledX = ((scaledX-di.absX.minValue)
                                     / di.absX.range)
                                 * (mDisplay.getWidth()-1);
                     }
                     if (di.absY != null) {
                         scaledY = ((scaledY-di.absY.minValue)
                                     / di.absY.range)
                                 * (mDisplay.getHeight()-1);
                     }
                     if (di.absPressure != null) {
                         scaledPressure = 
                             ((absPressure-di.absPressure.minValue)
                                     / (float)di.absPressure.range);
                     }
                     if (di.absSize != null) {
                         scaledSize = 
                             ((absSize-di.absSize.minValue)
                                     / (float)di.absSize.range);
                     }
                 }
#endif

                 unsigned long long whenMS = pEvent->when / 1000000;
                 AndroidMotionEvent(whenMS, action,
                         scaledX, scaledY, scaledPressure, scaledSize,
                         pEvent->deviceId);
                 lastAbsX = absX;
                 lastAbsY = absY;
            }
        }
        break;
        
        default:
            break;
        }
        gpEventQueue->recycleEvent(pEvent);
    }
}

int main(int argc, char** argv) {
    fprintf(stderr, "Welcome to stand-alone Android quake.\n");
    AndroidInitArgs(argc, argv);
    
    int result = init(argc, argv);
    if (result) {
        return result;
    }
    
    if (!AndroidInit()) {
        return 1;
    }

    while(true) {
        AndroidStep(gDisplayWidth, gDisplayHeight);
        checkGLError("AndroidStep");
        eglSwapBuffers(gDisplay, gSurface);
        checkEGLError("eglSwapBuffers");
        checkEvents();
    }
    return 0;
}
