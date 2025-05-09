import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

public class AudioConvey extends JFrame {

    private JTextField inputFileField;
    private JTextField outputFileField;
    private JComboBox<String> formatComboBox;

    public AudioConvey() {
        setTitle("音频格式转换器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 250);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 输入音频文件选择
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputFileField = new JTextField();
        JButton inputButton = new JButton("选择音频文件");
        inputPanel.add(inputFileField, BorderLayout.CENTER);
        inputPanel.add(inputButton, BorderLayout.EAST);

        inputButton.addActionListener((ActionEvent e) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("选择音频文件");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "音频文件 (*.mp3, *.wav, *.ogg, *.aac)", "mp3", "wav", "ogg", "aac"));
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                inputFileField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        // 格式选择
        JPanel formatPanel = new JPanel(new BorderLayout(5, 5));
        formatComboBox = new JComboBox<>(new String[]{"flac", "mp3", "wav", "aac"});
        formatPanel.add(new JLabel("选择输出格式:"), BorderLayout.WEST);
        formatPanel.add(formatComboBox, BorderLayout.CENTER);

        // 输出文件路径选择
        JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
        outputFileField = new JTextField();
        JButton outputButton = new JButton("选择导出路径");
        outputPanel.add(outputFileField, BorderLayout.CENTER);
        outputPanel.add(outputButton, BorderLayout.EAST);

        outputButton.addActionListener((ActionEvent e) -> {
            String selectedFormat = (String) formatComboBox.getSelectedItem();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择导出音频文件路径");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            // 从输入路径提取文件名并设置默认输出路径
            String inputFilePath = inputFileField.getText().trim();
            File inputFile = new File(inputFilePath);
            String outputFileName = inputFile.getName();
            // 去掉扩展名并加上目标格式
            String baseName = outputFileName.substring(0, outputFileName.lastIndexOf("."));
            String outputFileNameWithExt = baseName + "." + selectedFormat;

            fileChooser.setSelectedFile(new File(outputFileNameWithExt));

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                outputFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        // 转换按钮
        JButton convertButton = new JButton("开始转换");
        convertButton.addActionListener((ActionEvent e) -> {
            String inputPath = inputFileField.getText().trim();
            String outputPath = outputFileField.getText().trim();
            String format = (String) formatComboBox.getSelectedItem();

            if (inputPath.isEmpty() || outputPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请选择输入文件和导出路径", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 自动添加扩展名（防止用户省略）
            if (!outputPath.toLowerCase().endsWith("." + format)) {
                outputPath += "." + format;
            }

            convertAudio(inputPath, outputPath, format);
        });

        panel.add(inputPanel);
        panel.add(formatPanel);
        panel.add(outputPanel);
        panel.add(convertButton);

        add(panel);
    }

    private void convertAudio(String inputPath, String outputPath, String format) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath);
             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath, 1)) {

            grabber.start();

            recorder.setAudioChannels(grabber.getAudioChannels());
            recorder.setSampleRate(grabber.getSampleRate());
            recorder.setFormat(format);
            recorder.setAudioBitrate(192000); // 可选
            recorder.start();

            Frame frame;
            while ((frame = grabber.grabFrame()) != null) {
                recorder.record(frame);
            }

            recorder.stop();
            grabber.stop();

            JOptionPane.showMessageDialog(this, "音频转换成功！", "完成", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "转换失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AudioConvey().setVisible(true));
    }
}
