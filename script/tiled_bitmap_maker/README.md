## Tiled bitmap files maker (Python)

### Setup
The requirements.txt file should list all Python libraries depend on, and they will be installed using:

```
$ pip install -r requirements.txt
```

### Usage
To create tiled bitmaps, you can execute command below.

```
$ python main.py \
    --image_file_path [file_path] \
    --tile_width 256 \
    --tile_height 256 \
    --stride_horizontal 256 \
    --stride_vertical 256 \
    --output_dir_path [dir_path]
```