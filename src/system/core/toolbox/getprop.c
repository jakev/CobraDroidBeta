#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Start CobraDroid Modifications */
#define MOD_LINE_SZ 20
#define BUILD_LINE_SZ 50
#define MOD_STATE_FILE_NAME "/data/data/com.jakev.emucore/modstate"
#define BUILD_FILE_NAME "/data/data/com.jakev.emucore/build.prop"
#define DOES_NOT_EXIST 1
/* End CobraDroid Modifications */

#include <cutils/properties.h>

#include <sys/system_properties.h>

/* Start CobraDroid Modifications */
int getState(char *state_id)
{
        FILE *mod_file;
        char line[MOD_LINE_SZ];
        int mod_id_len;

        mod_id_len = strlen(state_id);
        mod_file = fopen(MOD_STATE_FILE_NAME, "r");

        char state_id_full[mod_id_len + 2];

        strncpy(state_id_full, state_id, mod_id_len);
        state_id_full[mod_id_len] = ':';
        state_id_full[mod_id_len + 1] = '\0';

        if (mod_file == NULL)
                return 0;

        while(fgets(line, MOD_LINE_SZ, mod_file) != NULL)
        {
                //This line isnt even long even, skip
                if (strlen(line) <= mod_id_len+2)
                        continue;

                char subbuff[mod_id_len + 2];

                strncpy(subbuff, line, mod_id_len + 1);
                subbuff[mod_id_len + 1] = '\0';

                if (strcmp(subbuff, state_id_full) == 0)
                {
                        char state;
                        state = line[mod_id_len+1];
                        close(mod_file);
                        return (state == '1') ? 1 : 0;
                }
                else
                        continue;
        }

        close(mod_file);
        return 0;
}

int get_prop(char *name, char *value, char *default_value)
{
        FILE *build_file;
        char line[BUILD_LINE_SZ];
        int name_len;
        char *name_buffer;

        name_len = strlen(name);
        build_file = fopen(BUILD_FILE_NAME, "r");

        char name_full[name_len + 2];

        strncpy(name_full, name, name_len);
        name_full[name_len] = '=';
        name_full[name_len + 1] = '\0';

        if (build_file == NULL)
        {
                if (strcmp(default_value, "") == 0) 
			return DOES_NOT_EXIST;
       
		int default_value_len = strlen(default_value);
                strncpy(value, default_value, default_value_len);
                value[default_value_len]='\0';
                close(build_file);
                return 0;
        }


        while(fgets(line, BUILD_LINE_SZ, build_file) != NULL)
        {
                //Line isn't even long enough, skip. 
               if (strlen(line) <= name_len+2)
                        continue;

                char subbuff[name_len+2]; //Account for \0 and =

                strncpy(subbuff, line, name_len + 1);
                subbuff[name_len +1] = '\0';

                if (strcmp(subbuff, name_full) == 0)
                {
                        int line_len = strlen(line);
                        int value_len = (line_len - name_len) - 2;

                        strncpy(value, &line[name_len+1], value_len);
                        value[value_len] = '\0';
                        close(build_file);
                        return 0;
                }
                else
                        continue;
        }
	//if there is a default value, thats fine, set it, 
	// otherwise we need to return the ERROR
        if (strcmp(default_value, "") == 0)
		return DOES_NOT_EXIST;

	int default_value_len = strlen(default_value);
        strncpy(value, default_value, default_value_len);
        value[default_value_len]='\0';
        close(build_file);
        return 0;
}
/* End CobraDroid Modification */

static void proplist(const char *key, const char *name, 
                     void *user __attribute__((unused)))
{
    printf("[%s]: [%s]\n", key, name);
}

int __system_property_wait(prop_info *pi);

int getprop_main(int argc, char *argv[])
{
    int n = 0;

    if (argc == 1) {
        (void)property_list(proplist, NULL);
    }
    else 
    {
        char value[PROPERTY_VALUE_MAX];
        char *default_value;
        if(argc > 2) {
            default_value = argv[2];
        } else {
            default_value = "";
        }

        int state = 0;
	char *build_mod = "build";

        state = getState(build_mod);

        if (state == 1) {
            int rtn = get_prop(argv[1], value, default_value);

	    if (rtn == DOES_NOT_EXIST)
	        property_get(argv[1], value, default_value);
	}
        else 
        {
            property_get(argv[1], value, default_value);
	}

        printf("%s\n", value);
    }
    return 0;
}
