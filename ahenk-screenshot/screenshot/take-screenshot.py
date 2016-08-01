#!/usr/bin/python3
# -*- coding: utf-8 -*-
# Author: Mine DOGAN <mine.dogan@agem.com.tr>


from base.plugin.abstract_plugin import AbstractPlugin
import json


class TakeScreenshot(AbstractPlugin):
    def __init__(self, task, context):
        super(TakeScreenshot, self).__init__()
        self.task = task
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

        self.temp_file_name = str(self.generate_uuid())
        self.shot_path = '{0}{1}'.format(str(self.Ahenk.received_dir_path()), self.temp_file_name)
        self.take_screenshot = '/bin/bash {0}screenshot/scripts/screenshot.sh {1}'.format(self.Ahenk.plugins_path(), self.shot_path)

        self.logger.debug('[SCREENSHOT] Instance initialized.')

    def handle_task(self):
        try:
            self.logger.debug('[SCREENSHOT] Executing command of taking screenshot.')
            self.execute(self.take_screenshot, result=True)
            self.logger.debug('[SCREENSHOT] Screenshot command was executed')

            if self.is_exist(self.shot_path):
                self.logger.debug('[SCREENSHOT] Shot founded.')

                data = {}
                md5sum = self.get_md5_file(str(self.shot_path))
                self.logger.debug('[SCREENSHOT] {0} renaming to {1}'.format(self.temp_file_name, md5sum))
                self.rename_file(self.shot_path, self.Ahenk.received_dir_path() + '/' + md5sum)
                self.logger.debug('[SCREENSHOT] Renamed.')
                data['md5'] = md5sum
                json_data = json.dumps(data)
                self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                             message='Screenshot görevi başarıyla çalıştırıldı.',
                                             data=json_data, content_type=self.get_content_type().IMAGE_JPEG.value)
                self.logger.debug('[SCREENSHOT] SCREENSHOT task is handled successfully')
            else:
                raise Exception('Image not found this path: {}'.format(self.shot_path))

        except Exception as e:
            self.logger.error('[SCREENSHOT] A problem occured while handling SCREENSHOT task: {0}'.format(str(e)))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Screenshot görevi çalıştırılırken bir hata oluştu.')


def handle_task(task, context):
    screenshot = TakeScreenshot(task, context)
    screenshot.handle_task()
