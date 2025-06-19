package finalproject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.List;

public class Wildareu {
    // 사용자의 선택(장소, 생존 여부, 공격성 등)을 저장하는 변수
    private String place = "";
    private boolean isAlive = true;
    private boolean isAggressive = false;

    // 주요 UI 컴포넌트와 데이터 저장 변수 선언
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JLabel resultLabel;
    private ScaledImagePanel imagePanel;
    private JTable infoTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<DisasterData> allDisasterData;

    // 생성자: 프로그램 실행 시 UI 및 화면 구성
    public Wildareu() {
        allDisasterData = new ArrayList<>();
        initializeFrame(); // 프레임과 레이아웃 초기화
        createMainScreen(); // 메인 화면 생성
        createGuideScreens(); // 상황별 행동 가이드 화면 생성
        createInfoScreen(); // 출현 정보(테이블) 화면 생성
        createWildlifeInfoScreen(); // 야생동물 정보 화면 생성
        frame.add(mainPanel);
        cardLayout.show(mainPanel, "main"); // 처음엔 메인화면 표시
        frame.setVisible(true);
    }

    // JFrame 및 CardLayout 초기화
    private void initializeFrame() {
        frame = new JFrame("Wild are U? 야생동물 대응 가이드");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 700);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
    }

    // 이미지를 패널 크기에 맞게 그려주는 커스텀 패널 클래스
    static class ScaledImagePanel extends JPanel {
        private Image image;

        public ScaledImagePanel(String imagePath) {
            setImage(imagePath);
        }

        public void setImage(String imagePath) {
            if (imagePath == null || imagePath.isEmpty()) {
                image = null;
                repaint();
                return;
            }
            File f = new File(imagePath);
            if (f.exists()) {
                image = new ImageIcon(imagePath).getImage();
            } else {
                image = null;
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                // 이미지 비율 유지하며 패널에 맞게 그림
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                int imgWidth = image.getWidth(this);
                int imgHeight = image.getHeight(this);

                double imgAspect = (double) imgWidth / imgHeight;
                double panelAspect = (double) panelWidth / panelHeight;

                int drawWidth, drawHeight;
                if (panelAspect > imgAspect) {
                    drawHeight = panelHeight;
                    drawWidth = (int) (panelHeight * imgAspect);
                } else {
                    drawWidth = panelWidth;
                    drawHeight = (int) (panelWidth / imgAspect);
                }
                int x = (panelWidth - drawWidth) / 2;
                int y = (panelHeight - drawHeight) / 2;
                g.drawImage(image, x, y, drawWidth, drawHeight, this);
            }
        }
    }

    // 메인 화면 (타이틀, 메뉴 버튼 등) 생성
    private void createMainScreen() {
        JPanel mainScreen = new JPanel();
        mainScreen.setLayout(new BoxLayout(mainScreen, BoxLayout.Y_AXIS));
        mainScreen.setBackground(Color.WHITE);

        ScaledImagePanel mainImagePanel = new ScaledImagePanel("main.png");
        mainImagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainImagePanel.setPreferredSize(new Dimension(400, 300));
        mainImagePanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 350));
        mainImagePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel titleLabel = new JLabel("Wild are U?");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("야생동물 대응 가이드");
        subtitleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton guideButton = createStyledButton("야생동물을 마주했다면?");
        JButton infoButton = createStyledButton("야생동물 출현 정보");
        JButton wildlifeInfoButton = createStyledButton("주요 야생동물 정보");
        JButton exitButton = createStyledButton("종료");

        // 각 버튼 클릭 시 화면 전환
        guideButton.addActionListener(e -> cardLayout.show(mainPanel, "alive"));
        infoButton.addActionListener(e -> {
            loadCsvData(); // CSV 데이터 로드
            cardLayout.show(mainPanel, "info");
        });
        wildlifeInfoButton.addActionListener(e -> cardLayout.show(mainPanel, "wildlife"));
        exitButton.addActionListener(e -> System.exit(0));

        mainScreen.add(Box.createVerticalStrut(20));
        mainScreen.add(titleLabel);
        mainScreen.add(Box.createVerticalStrut(10));
        mainScreen.add(subtitleLabel);
        mainScreen.add(Box.createVerticalStrut(10));
        mainScreen.add(mainImagePanel);
        mainScreen.add(Box.createVerticalStrut(20));
        mainScreen.add(guideButton);
        mainScreen.add(Box.createVerticalStrut(10));
        mainScreen.add(infoButton);
        mainScreen.add(Box.createVerticalStrut(10));
        mainScreen.add(wildlifeInfoButton);
        mainScreen.add(Box.createVerticalStrut(10));
        mainScreen.add(exitButton);
        mainScreen.add(Box.createVerticalStrut(20));
        mainPanel.add(mainScreen, "main");
    }

    // 버튼 스타일 통일을 위한 메서드
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(200, 40));
        button.setMaximumSize(new Dimension(200, 40));
        button.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    // 출현 정보(재난문자 데이터) 화면 생성
    private void createInfoScreen() {
        JPanel infoScreen = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        JButton backButton = new JButton("메인으로");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "main"));

        // 검색창 생성 및 플레이스홀더 설정
        searchField = new JTextField("지역별로 보고 싶다면 여기에 지역 이름 입력", 20);
        searchField.setForeground(Color.GRAY);

        // 검색창 포커스 이벤트(플레이스홀더 처리)
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("지역별로 보고 싶다면 여기에 지역 이름 입력")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText("지역별로 보고 싶다면 여기에 지역 이름 입력");
                }
            }
        });

        // 검색창 입력 변화 시 테이블 필터링
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }
        });

        topPanel.add(backButton);
        topPanel.add(new JLabel("검색: "));
        topPanel.add(searchField);

        // 테이블 컬럼명 지정 및 모델 생성
        String[] columnNames = { "NO", "메시지내용", "수신지역", "등록일자" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        infoTable = new JTable(tableModel);
        infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        sorter = new TableRowSorter<>(tableModel);
        infoTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(infoTable);
        scrollPane.setPreferredSize(new Dimension(450, 300));

        infoScreen.add(topPanel, BorderLayout.NORTH);
        infoScreen.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(infoScreen, "info");
    }

    // 검색창 입력에 따라 테이블 데이터 필터링
    private void filterTable() {
        String text = searchField.getText();
        if (text.equals("지역별로 보고 싶다면 여기에 지역 이름 입력") || text.trim().length() == 0) {
            displayFilteredData("");
        } else {
            displayFilteredData(text.trim());
        }
    }

    // '출몰' 또는 '출현' 포함 + 지역명 필터링 후 테이블에 표시
    private void displayFilteredData(String regionFilter) {
        tableModel.setRowCount(0);
        for (DisasterData data : allDisasterData) {
            if ((data.getMessageContent().contains("출몰") || data.getMessageContent().contains("출현")) &&
                    (regionFilter.isEmpty() || data.getReceptionRegion().contains(regionFilter))) {
                tableModel.addRow(new Object[] {
                        data.getNo(),
                        data.getMessageContent(),
                        data.getReceptionRegion(),
                        data.getRegistrationDate()
                });
            }
        }
    }

    // CSV 파일을 읽어와 allDisasterData 리스트에 저장
    private void loadCsvData() {
        tableModel.setRowCount(0);
        allDisasterData.clear();
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    readCsvFile();
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame,
                                "CSV 파일을 읽는 중 오류가 발생했습니다: " + e.getMessage(),
                                "오류", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                displayFilteredData("");
            }
        };
        worker.execute();
    }

    // 실제로 CSV 파일을 한 줄씩 읽어서 DisasterData 객체로 변환
    private void readCsvFile() throws Exception {
        File file = new File("행정안전부_전체재난문자.csv");
        if (!file.exists()) {
            throw new FileNotFoundException("CSV 파일을 찾을 수 없습니다.");
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                // 첫 줄(헤더) 건너뜀
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                // CSV 한 줄 파싱
                String[] values = parseCsvLine(line);
                if (values.length >= 4) {
                    DisasterData data = new DisasterData(
                            values[0], // NO
                            values[1], // 메시지내용
                            values[2], // 수신지역
                            values[3] // 등록일자
                    );
                    allDisasterData.add(data);
                }
            }
        }
    }

    // CSV 한 줄을 쉼표, 따옴표 처리해서 분리
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        result.add(currentField.toString());

        return result.toArray(new String[0]);
    }

    // 주요 야생동물 정보 화면 생성
    private void createWildlifeInfoScreen() {
        JPanel wildlifePanel = new JPanel();
        wildlifePanel.setLayout(new BoxLayout(wildlifePanel, BoxLayout.Y_AXIS));
        wildlifePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("보고 싶은 주요 야생동물 정보를 선택하세요.");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton backButton = new JButton("메인으로");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "main"));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));
        buttonPanel.setMaximumSize(new Dimension(300, 120));

        String[] animals = { "멧돼지", "너구리", "고라니" };
        for (String animal : animals) {
            JButton btn = createStyledButton(animal);
            btn.addActionListener(e -> showAnimalInfo(animal));
            buttonPanel.add(btn);
        }

        wildlifePanel.add(Box.createVerticalStrut(30));
        wildlifePanel.add(titleLabel);
        wildlifePanel.add(Box.createVerticalStrut(30));
        wildlifePanel.add(buttonPanel);
        wildlifePanel.add(Box.createVerticalStrut(30));
        wildlifePanel.add(backButton);

        mainPanel.add(wildlifePanel, "wildlife");
    }

    // 동물 정보 및 울음소리 안내 화면 표시
    private void showAnimalInfo(String animal) {
        String info = "";
        String imagePath = animal + ".jpg";
        String soundUrl = "";

        switch (animal) {
            case "멧돼지":
                info = "<html><div style='text-align: center; padding: 10px;'>"
                        + "<b>멧돼지 관련 정보</b><br><br>"
                        + "1. 멧돼지는 머리가 크고 목이 짧아요. 주둥이가 원통형이고 다리가 짧아요.<br>"
                        + "2. 자극에 약해요. 도망칠 때 절대 뛰거나 소리를 지르거나 등을 보이지 마세요.<br>"
                        + "3. 눈을 마주치면 안돼요. 물건을 던져서도 안돼요.<br>"
                        + "4. 산에 사는 야생 멧돼지는 주로 해뜨기 전 이른 아침이나 해질 무렵에 활동해요. 해당 시간에 유의해요.</div></html>";
                soundUrl = "https://youtube.com/shorts/SnZ-1ivegpo?si=owgKkuhmKHzFIPgT";
                break;
            case "너구리":
                info = "<html><div style='text-align: center; padding: 10px;'>"
                        + "<b>너구리 관련 정보</b><br><br>"
                        + "1. 너구리는 덤불 같은 긴 꼬리를 가졌어요. 몸 길이가 50~70cm 면 꼬리는 그의 3분의 1 정도 길이예요.<br>"
                        + "2. 야생 너구리는 피부 질환과 광견병의 매개체예요. 절대 만지면 안돼요.<br>"
                        + "3. 반려동물과 동반시 너구리가 반려동물을 공격할 수 있어요. 목줄을 필수 착용하고 주의해주세요.<br>"
                        + "4. 너구리는 잡식성이라 뭐든 먹어요. 도심 속 쓰레기통을 뒤지지 않도록 밀폐가 중요해요.</div></html>";
                soundUrl = "https://youtube.com/shorts/1UyaH0yBrmM?si=i_nu6IWQyJBcfGIa";
                break;
            case "고라니":
                info = "<html><div style='text-align: center; padding: 10px;'>"
                        + "<b>고라니 관련 정보</b><br><br>"
                        + "1. 고라니는 뿔 대신 송곳니가 길고 날카롭게 발달했어요. 노루와 달리 몸집이 더 작고 꼬리가 길어요.<br>"
                        + "2. 외진 길에서 야생 고라니를 마주할 경우, 자극하지 않는 게 중요해요. 자극하지 않으면 대부분 고라니가 피해가요.<br>"
                        + "3. 도로에서 고라니를 마주할 경우, 낮은 속도로 서행하고 야간일 경우 더욱 주의하는 게 중요해요. 클락션은 울리지 않아요.</div></html>";
                soundUrl = "https://youtube.com/shorts/oVRXDx-Xqlg?si=CM4T8l9G9VD0lLMt";
                break;
        }
        imagePanel.setImage(imagePath);
        resultLabel.setText(info);

        JPanel resultPanel = (JPanel) imagePanel.getParent();
        JButton soundButton = null;
        for (Component comp : resultPanel.getComponents()) {
            if (comp instanceof JButton && "울음소리 듣기".equals(((JButton) comp).getText())) {
                soundButton = (JButton) comp;
                break;
            }
        }
        if (soundButton == null) {
            soundButton = new JButton("울음소리 듣기");
            soundButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultPanel.add(soundButton, resultPanel.getComponentCount() - 1);
        }
        for (ActionListener al : soundButton.getActionListeners()) {
            soundButton.removeActionListener(al);
        }
        String finalSoundUrl = soundUrl;
        soundButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI(finalSoundUrl));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "브라우저를 열 수 없습니다: " + ex.getMessage());
            }
        });

        resultPanel.revalidate();
        resultPanel.repaint();

        cardLayout.show(mainPanel, "result");
    }

    // 상황별 행동 가이드(질문-선택-결과) 화면 생성
    private void createGuideScreens() {
        JPanel alivePanel = new JPanel();
        alivePanel.setLayout(new BoxLayout(alivePanel, BoxLayout.Y_AXIS));
        alivePanel.setBackground(Color.WHITE);

        JLabel aliveLabel = new JLabel("야생동물이 살아있나요?");
        aliveLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        aliveLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnAliveYes = createStyledButton("예");
        JButton btnAliveNo = createStyledButton("아니오");

        // 생존 여부에 따라 다음 단계 이동
        btnAliveYes.addActionListener(e -> {
            isAlive = true;
            cardLayout.show(mainPanel, "place");
        });
        btnAliveNo.addActionListener(e -> {
            isAlive = false;
            cardLayout.show(mainPanel, "place");
        });

        alivePanel.add(Box.createVerticalStrut(40));
        alivePanel.add(aliveLabel);
        alivePanel.add(Box.createVerticalStrut(20));
        alivePanel.add(btnAliveYes);
        alivePanel.add(Box.createVerticalStrut(10));
        alivePanel.add(btnAliveNo);
        mainPanel.add(alivePanel, "alive");

        JPanel placePanel = new JPanel();
        placePanel.setLayout(new BoxLayout(placePanel, BoxLayout.Y_AXIS));
        placePanel.setBackground(Color.WHITE);

        JLabel placeLabel = new JLabel("어디서 야생동물을 목격하셨나요?");
        placeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        placeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        placePanel.add(Box.createVerticalStrut(30));
        placePanel.add(placeLabel);
        placePanel.add(Box.createVerticalStrut(20));

        String[] places = { "도로", "공원", "주택가", "기타" };
        for (String p : places) {
            JButton btn = createStyledButton(p);
            btn.addActionListener(e -> {
                place = p;
                if (!isAlive) {
                    showResult();
                } else {
                    cardLayout.show(mainPanel, "aggressive");
                }
            });
            placePanel.add(Box.createVerticalStrut(10));
            placePanel.add(btn);
        }

        JButton backToMain = new JButton("메인으로");
        backToMain.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToMain.addActionListener(e -> cardLayout.show(mainPanel, "main"));
        placePanel.add(Box.createVerticalStrut(20));
        placePanel.add(backToMain);

        mainPanel.add(placePanel, "place");

        JPanel aggressivePanel = new JPanel();
        aggressivePanel.setLayout(new BoxLayout(aggressivePanel, BoxLayout.Y_AXIS));
        aggressivePanel.setBackground(Color.WHITE);

        JLabel aggressiveLabel = new JLabel("공격적인 성향을 보이나요?");
        aggressiveLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        aggressiveLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnAggYes = createStyledButton("예");
        JButton btnAggNo = createStyledButton("아니오");

        btnAggYes.addActionListener(e -> {
            isAggressive = true;
            showResult();
        });
        btnAggNo.addActionListener(e -> {
            isAggressive = false;
            showResult();
        });

        aggressivePanel.add(Box.createVerticalStrut(40));
        aggressivePanel.add(aggressiveLabel);
        aggressivePanel.add(Box.createVerticalStrut(20));
        aggressivePanel.add(btnAggYes);
        aggressivePanel.add(Box.createVerticalStrut(10));
        aggressivePanel.add(btnAggNo);
        mainPanel.add(aggressivePanel, "aggressive");

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(Color.WHITE);

        resultLabel = new JLabel("결과 안내문이 여기에 표시됩니다.", SwingConstants.CENTER);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        imagePanel = new ScaledImagePanel(null);
        imagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imagePanel.setPreferredSize(new Dimension(350, 220));
        imagePanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 250));
        imagePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JButton btnRestart = new JButton("처음으로 돌아가기");
        btnRestart.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRestart.addActionListener(e -> {
            place = "";
            isAlive = true;
            isAggressive = false;
            imagePanel.setImage(null);
            cardLayout.show(mainPanel, "main");
        });

        resultPanel.add(Box.createVerticalStrut(20));
        resultPanel.add(resultLabel);
        resultPanel.add(Box.createVerticalStrut(20));
        resultPanel.add(imagePanel);
        resultPanel.add(Box.createVerticalStrut(30));
        resultPanel.add(btnRestart);
        mainPanel.add(resultPanel, "result");
    }

    // 선택한 상황에 따라 행동 가이드 안내문 및 이미지 표시
    private void showResult() {
        String guide = "";
        imagePanel.setImage(null);

        String imagePath = null;
        if (place.equals("도로") && !isAlive) {
            imagePath = "call.png";
        } else if (isAlive) {
            imagePath = "live.png";
        }
        imagePanel.setImage(imagePath);

        if (place.equals("도로")) {
            if (!isAlive) {
                guide = "<html><div style='text-align: center; padding: 10px;'><b>[도로/사체]</b><br><br>"
                        + "1. 차량을 안전하게 정차하세요<br>"
                        + "2. 동물에 접근하지 마세요<br>"
                        + "3. 112 또는 관할 부서에 신고하세요</div></html>";
            } else if (isAggressive) {
                guide = "<html><div style='text-align: center; padding: 10px;'><b>[도로/공격적]</b><br><br>"
                        + "1. 차량에서 내리지 마세요<br>"
                        + "2. 즉시 신고하세요</div></html>";
            } else {
                guide = "<html><div style='text-align: center; padding: 10px;'><b>[도로/비공격적]</b><br><br>"
                        + "1. 안전 거리 유지<br>"
                        + "2. 관할 부서에 신고</div></html>";
            }
        } else {
            if (!isAlive) {
                guide = "<html><div style='text-align: center; padding: 10px;'><b>[" + place + "/사체]</b><br><br>"
                        + "1. 관할 부서에 신고하여 조치를 요청하세요</div></html>";
            } else if (isAggressive) {
                guide = "<html><div style='text-align: center; padding: 10px;'><b>[" + place + "/공격적]</b><br><br>"
                        + "1. 동물에 접근하지 마세요<br>"
                        + "2. 안전한 곳으로 이동하세요<br>"
                        + "3. 신고하세요</div></html>";
            } else {
                guide = "<html><div style='text-align: center; padding: 10px;'><b>[" + place + "/비공격적]</b><br><br>"
                        + "1. 동물과 거리를 유지하세요<br>"
                        + "2. 관할 부서에 신고하세요</div></html>";
            }
        }

        resultLabel.setText(guide);
        cardLayout.show(mainPanel, "result");
    }

    // 프로그램 시작점
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Wildareu());
    }
}

// 재난문자 데이터 저장용 클래스
class DisasterData {
    private String no;
    private String messageContent;
    private String receptionRegion;
    private String registrationDate;

    public DisasterData(String no, String messageContent, String receptionRegion, String registrationDate) {
        this.no = no;
        this.messageContent = messageContent;
        this.receptionRegion = receptionRegion;
        this.registrationDate = registrationDate;
    }

    public String getNo() {
        return no;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getReceptionRegion() {
        return receptionRegion;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }
}
