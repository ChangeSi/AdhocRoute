#include "com_xd_adhocroute_nativehelper_NativeTask.h"
#include <stdio.h>
#include <sys/syscall.h>
#include "sys/types.h"
#include <errno.h>
#include <stdarg.h>
#include <stdio.h>
#include <dirent.h>
#include <fcntl.h>
#include <signal.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include <sys/mount.h>
#include <sys/stat.h>
#include <sys/syscall.h>
#include <sys/types.h>
#include <sys/wait.h>

#include <unistd.h>

#include <netinet/in.h>
#include <arpa/inet.h>

#include <linux/wireless.h>

#include <sys/system_properties.h>
#include <android/log.h>

#include <linux/in.h>
#define PROPERTY_KEY_MAX   32
#define PROPERTY_VALUE_MAX  92

const int READ_BUF_SIZE = 50;
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "AdhocRoute", __VA_ARGS__)

JNIEXPORT jstring JNICALL Java_com_xd_adhocroute_nativehelper_NativeTask_getProp
  (JNIEnv *env, jclass class, jstring name)
{
  const char *nameString;
  nameString = (*env)->GetStringUTFChars(env, name, 0);

  char value[PROPERTY_VALUE_MAX];
  char *default_value;
  jstring jstrOutput;

  default_value = "undefined";
  property_get(nameString, value, default_value);

  jstrOutput = (*env)->NewStringUTF(env, value);

  (*env)->ReleaseStringUTFChars(env, name, nameString);

  return jstrOutput;
}

JNIEXPORT jint JNICALL Java_com_xd_adhocroute_nativehelper_NativeTask_runCommand
  (JNIEnv *env, jclass class, jstring command)
{
  const char *commandString;
  commandString = (*env)->GetStringUTFChars(env, command, 0);
  int exitcode = system(commandString);
  (*env)->ReleaseStringUTFChars(env, command, commandString);
  return (jint)exitcode;
}

int property_get(const char *key, char *value, const char *default_value)
{
    int len;

    len = __system_property_get(key, value);
    if(len > 0) {
        return len;
    }

    if(default_value) {
        len = strlen(default_value);
        memcpy(value, default_value, len + 1);
    }
    return len;
}

JNIEXPORT jint JNICALL Java_com_xd_adhocroute_nativehelper_NativeTask_killProcess
  (JNIEnv * env, jclass class, jint param, jstring procName)
{
	const char *procNameChar;
	procNameChar = (*env)->GetStringUTFChars(env, procName, 0);
	int exitcode = kill_processes_by_name((int)param, procNameChar);
	(*env)->ReleaseStringUTFChars(env, procName, procNameChar);
	return (jint)exitcode;
}

int kill_processes_by_name(int parameter, const char* processName) {
        int returncode = 0;

        DIR *dir = NULL;
        struct dirent *next;

        // open /proc
        dir = opendir("/proc");
        if (!dir)
                fprintf(stderr, "Can't open /proc \n");

        while ((next = readdir(dir)) != NULL) {
                FILE *status = NULL;
                char filename[READ_BUF_SIZE];
                char buffer[READ_BUF_SIZE];
                char name[READ_BUF_SIZE];

                /* Must skip ".." since that is outside /proc */
                if (strcmp(next->d_name, "..") == 0)
                        continue;

                sprintf(filename, "/proc/%s/status", next->d_name);
                if (! (status = fopen(filename, "r")) ) {
                        continue;
                }
                if (fgets(buffer, READ_BUF_SIZE-1, status) == NULL) {
                        fclose(status);
                        continue;
                }
                fclose(status);

                /* Buffer should contain a string like "Name:   binary_name" */
                sscanf(buffer, "%*s %s", name);

                LOGV("进程Name-->%s", name);
                LOGV("进程processName-->%s", processName);
                if ((strstr(name, processName)) != NULL) {
                        // Trying to kill
                	LOGV("进程-->执行到这里");
                        int signal = kill(strtol(next->d_name, NULL, 0), parameter);
                        if (signal != 0) {
                                fprintf(stderr, "Unable to kill process %s (%s)\n",name, next->d_name);
                                returncode = -1;
                        }
                }
        }
        closedir(dir);
        return returncode;
}


