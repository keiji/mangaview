#!/bin/python

import os
from math import ceil

from PIL import Image
from absl import app
from absl import flags

FLAGS = flags.FLAGS
flags.DEFINE_string("image_file_path", None, "Image file path that make tiled images you want.")
flags.DEFINE_integer("tile_width", 256, "Width of one tile.")
flags.DEFINE_integer("tile_height", 256, "Height of one tile.")
flags.DEFINE_integer("stride_horizontal", None, "The stride of the tiling along the width.")
flags.DEFINE_integer("stride_vertical", None, "The stride of the tiling along the height.")
flags.DEFINE_string("output_dir_path", None, "Output directory path.")


class TiledSource:
    def __init__(self, sourceWidth, sourceHeight, colCount, rowCount):
        self.sourceWidth = sourceWidth
        self.sourceHeight = sourceHeight
        self.colCount = colCount
        self.rowCount = rowCount
        self.tile_list = []


def build(image, tile_width, tile_height, stride_horizontal, stride_vertical):
    (width, height) = image.size

    print('tile_width : %d' % tile_width)
    print('tile_height : %d' % tile_height)
    print('stride_horizontal : %d' % stride_horizontal)
    print('stride_vertical: %d' % stride_vertical)

    assert width > tile_width, '--tile_width must not be greater than image width.'
    assert height > tile_height, '--tile_height must not be greater than image height.'
    assert stride_horizontal <= tile_width, '--stride_horizontal must not be greater than --tile_width.'
    assert stride_vertical <= tile_height, '--stride_vertical must not be greater than --tile_height.'

    col_count = round(ceil(width / stride_horizontal))
    row_count = round(ceil(height / stride_vertical))

    print('col_count:%d, row_count:%d' % (col_count, row_count))

    tiled_source = TiledSource(width, height, col_count, row_count)

    for y in range(row_count):
        for x in range(col_count):
            left = x * stride_horizontal
            right = left + tile_width
            top = y * stride_vertical
            bottom = top + tile_height

            right = min(right, width)
            bottom = min(bottom, height)

            tiled_source.tile_list.append((left, top, right, bottom))

    return tiled_source


def main(argv):
    del argv  # Unused.

    assert FLAGS.image_file_path is not None, "--image_file_path must be specified."
    assert os.path.exists(FLAGS.image_file_path), "image_file_path %s not found".format(FLAGS.image_file_path)

    assert FLAGS.output_dir_path is not None, "--output_dir_path must be specified."
    assert os.path.exists(FLAGS.output_dir_path), "output_dir_path %s not found".format(FLAGS.output_dir_path)

    base_name = os.path.basename(FLAGS.image_file_path)
    name, ext = os.path.splitext(base_name)
    output_dir = os.path.join(FLAGS.output_dir_path, name)
    os.makedirs(output_dir, exist_ok=True)

    print('output directory : %s' % output_dir)

    stride_horizontal = FLAGS.stride_horizontal
    stride_vertical = FLAGS.stride_vertical

    if stride_horizontal is None:
        stride_horizontal = FLAGS.tile_width
    if stride_vertical is None:
        stride_vertical = FLAGS.tile_height

    with Image.open(open(FLAGS.image_file_path, mode='br')) as image:
        tiled_source = build(image, FLAGS.tile_width, FLAGS.tile_height, stride_horizontal, stride_vertical)
        for index, rect in enumerate(tiled_source.tile_list):
            file_name = "%s-%04d%s" % (name, index, ext)
            output_path = os.path.join(output_dir, file_name)
            image.crop(rect).save(output_path)

    print('done.')


if __name__ == '__main__':
    app.run(main)
