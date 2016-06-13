#!/usr/bin/python3
# -*- coding: utf-8 -*-
# Author: Mine DOGAN <mine.dogan@agem.com.tr>


import datetime

from base.model.enum.ContentType import ContentType
from base.plugin.abstract_plugin import AbstractPlugin


class TakeScreenshot(AbstractPlugin):
    def __init__(self, task, context):
        super(TakeScreenshot, self).__init__()
        self.task = task
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

        self.shot_path = '/tmp/ahenk_screenshot_' + str(datetime.datetime.now().strftime("%d%m%Y%I%M")) + '.jpg'
        self.take_screenshot = '/bin/bash ./plugins/screenshot/scripts/screenshot.sh ' + self.shot_path

    def handle_task(self):
        try:
            self.execute(self.take_screenshot)

            self.scope.getMessager().send_file(self.shot_path)

            data = {}
            md5sum = self.get_md5_file(str(self.shot_path))
            data['md5'] = md5sum
            print('md5: ' + md5sum)

            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='User screenshot task processed successfully',
                                         data=data, content_type=ContentType.APPLICATION_JSON.value)
            self.logger.info('[SCREENSHOT] SCREENSHOT task is handled successfully')

        except Exception as e:
            self.logger.error('[SCREENSHOT] A problem occured while handling SCREENSHOT task: {0}'.format(str(e)))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='A problem occured while handling SCREENSHOT task: {0}'.format(str(e)))


def handle_task(task, context):
    screenshot = TakeScreenshot(task, context)
    screenshot.handle_task()
