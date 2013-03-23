package cityfind;

import cityfind.gasfind.GasDecisionProcess;
import cityfind.gui.*;


import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.lang.ClassLoader;
import javax.swing.ImageIcon;

import spa.DecisionProcess;
import spa.ShopElement;
import spa.question.Question;
import sun.awt.image.URLImageSource;
/**
 * <p>Title: CityFinder: Mobile city search demo application</p>
 *
 * <p>
 * <b>Description:</b>
 * 					This demo application lets a user find the best gas stations in Houston and Austin city 
 * 					based on his/her saved SPA-decisions.
 * <p>
 *  				It displays a map of Houston city downtown area, and
 *                 	provides 4 question panels to the user to collect his/her decisions.
 *                 	After the initial interactions (training), this application
 *                 	records and stores the user decisions for the one-click
 *                 	automation search.   
 *<p>
 *                 	<b>CityFinder is powered by the SPA Algorithm.  https://github.com/chonc/spa</b>
 *<p>
 *<b>Note:</b>     	The 'spa' and 'cityfind.gasfind' are the core packages.
 *                 	The 'spa' package provides the framework. And,
 *                 	'cityfind.gasfind' provides a working example of
 *                 	how to extend and utilize the super ('spa') package.
 *                 	cityfind.gasfind.GasDecisionProcess and cityfind.gasfind.sax.GasSAXManager classes 
 *                 	are the good places to start learning about this application.</p>
 *                 
 *                 
 *<p> 
 *TODO            	1. put the network connection related methods into a separate class.
 *                 	2. Check the final data quality.  If the final data fail to meet the
 *                 	SPA data quality requirement, the application should do the more Decision processes.
 *
 *<p> 
 *Copyright:    	Copyright (c) 2001-2013 Chon Chung.
 *@author       	Chon Chung
 *@version      	Demo 0.7
 *
 *<p>          		License: Lesser General Public License (LGPL)
 */

public class CityFinderJWS extends Frame implements ActionListener{

  /** The start button click event.*/
  public static final String START_APP_EVENT = "Start";
  /** The Next Question button click event.*/
  public static final String QUE_NEXT_EVENT = "Next Question";
  /** The completion of collecting user decisions event.*/
  public static final String QUE_DONE_EVENT = "Collection Done";
  /** the one-click auto search button Event.*/
  public static final String ONE_CLICK_EVENT = "ONE CLICK";


  /** A panel displays the decision question to collect a user decision choice answer. */
  private QuestionPanel questionPanel;
  /** Reference of the DecisionProcess instance. */
  private static DecisionProcess decisionProcess;
  /** Canvas to display a city map. */
  private MapCanvas mapCanvas;
  /** A panel that displays the Houston city search results and 'search Austin' button. */
  private ToNextCityPanel toNextCityPanel;
  /** A panel displays the Austin city search progress status. */
  private OneClickStatusPanel austinSearchPanel;
  /** The start panel that displays this application GUI. */
  private StartPanel startPanel;
  /** URL, Internet connection validation status. */
  private boolean url_status;
  /** URL address of the resource files location*/
  private String urlStr;
  /** Houston Map name */
  private String houstonMap;
  /** Houston gas-stations data XML file */
  private String houstonXML;
  /** Austin Map name */
  private String austinMap;
  /** Austin gas-stations data XML file */
  private String austinXML;
						


  /**Constructor.  Initializes this properties, and displays
   * a starting window panel for the Houston city search.
   *
   * @param urlStr      URL address of the resource files location
   * @param houstonMap  Houston Map file name
   * @param houstonXML  Houston gas-stations information XML resource file
   * @param austinMap   Austin Map file name
   * @param austinXML   Austin gas-stations information XML resource file
   */
  public CityFinderJWS(String urlStr,
						String houstonMap, String houstonXML,
						String austinMap, String austinXML) {
	  this.urlStr = urlStr;
	  this.houstonMap = houstonMap;
	  this.houstonXML = houstonXML;
	  this.austinMap = austinMap;
	  this.austinXML = austinXML;

	  setLayout(new FlowLayout(FlowLayout.LEFT));

	  //for the window closing button event
	  addWindowListener(new WindowAdapter()
	  { public void windowClosing(WindowEvent e)
		  { System.exit(0);
		  }
	  });

	  initializeHoustonSearch();
  }

  /**
   * Gets the shop XML data file and initializes the SPA process for the
   * Houston city search.
   *
   * @param cityShopXML   the city gas-stations information XML file source
   */
  private void setSPA_Houston(String cityShopXML){
	  URL url=null;
	  if (decisionProcess == null){
			if (url_status){//if network file is available
				  try {
						  url = new URL (urlStr + cityShopXML);
						  decisionProcess = new GasDecisionProcess(url, cityShopXML);
						  startPanel.appendText(">> Received the business XML data file... \n" +
												">> Successfully initialized.  Click the Start button.");
						  startPanel.enableStartButton();
				  }catch(MalformedURLException em) {
						  try{
								// Get the local file instead.
									decisionProcess = new GasDecisionProcess(cityShopXML);		
								startPanel.appendText(">> Using a local data file... \n" +
                                                      ">> Successfully initialized.  Click the Start button.");
								startPanel.enableStartButton();
						  }catch(Exception ex){
							printError(startPanel, ex, url.toString());
								  return;
						  }
				  }catch (Exception  e ) {
					printError(startPanel, e, url.toString());
				  }
			}else{
				  try{
						// Get the local file instead.
						decisionProcess = new GasDecisionProcess(cityShopXML);	
						startPanel.appendText(">> Using a local data file... \n" +
                                              ">> Successfully initialized.  Click the Start button.");
						startPanel.enableStartButton();
				  }catch(Exception exc){
					printError(startPanel, exc, url.toString());
				  }
			}
	  }
  }
  
  /**
   * Gets the Austin city shop XML file, and resets the SPA process 
   * for One-click automation search.
   *
   * @param cityShopXML   the city gas-stations information XML source
   */
  private void setSPA_Austin(String cityShopXML){
	  URL url=null;

			if (url_status){
				  try {
						  url = new URL (urlStr + cityShopXML);
						  ((GasDecisionProcess)decisionProcess).reset(url, cityShopXML);
						  austinSearchPanel.appendText(">> Received the business XML file from the remote URL... \n" +
											 ">> Received the data XML file.");
				  }catch(MalformedURLException em) {
						  try{
								// If the remote file is not available, Get the local file instead.
								((GasDecisionProcess)decisionProcess).reset(cityShopXML);	
								austinSearchPanel.appendText(">> Received the data XML file.");
						  }catch(Exception ex){
							printError(austinSearchPanel, ex, url.toString());
								return;
						  }
				  }catch (Exception  e ) {
					printError(austinSearchPanel, e, url.toString());
				  }
			}else{
				  try{
						// If the remote file is not available, Get the local file instead.
						((GasDecisionProcess)decisionProcess).reset(cityShopXML);	
					  	austinSearchPanel.appendText(">> Received the data XML file.");
				  }catch(Exception exc){
					printError(austinSearchPanel, exc, url.toString());
				  }
			}
  }

  /**
   * By using user's recorded decisions, performs one-click automation search in
   * Austin city area, and displays the results.
   */
  public void oneClickAutomation(){
	  Question question;
	  austinSearchPanel.appendText("\n\n>>One click automation search:");

	  while(decisionProcess.getTaskStatus() != DecisionProcess.TASK_DONE) {
		  question = decisionProcess.getNextQuestion();
		  question.doAutoAction();
		  austinSearchPanel.appendText("\n>>" + question.toString());
		  austinSearchPanel.increaseProgressBar(20);//increase the bar size to indicate the progress
	  }
	  austinSearchPanel.increaseProgressBar(100);//increase the bar size to indicate the task completion.
	  displayAustinResults();
  }
    

  /**Based on the receives actionEvents, performs the next step. */
  public void actionPerformed(ActionEvent evt){
	//receives the start button event from the startPanel
	if (evt.getActionCommand().equals(START_APP_EVENT)) {
		displayHoustonSearch();

	//receives the next button event from the QuestionPanel
	}else if (evt.getActionCommand().equals(QUE_NEXT_EVENT)){
		((Question) questionPanel.getQuestion()).doAction();
		questionPanel.setNextQuestion(((GasDecisionProcess)decisionProcess).getNextPanelQuestion());

        //if it reached the final question.
		if (((Question) questionPanel.getQuestion()).getID() == 4)
			  questionPanel.setNextButActionCommand(QUE_DONE_EVENT);

	//receives the final button event from the QuestionPanel
	}else if (evt.getActionCommand().equals(QUE_DONE_EVENT)){
		((Question) questionPanel.getQuestion()).doAction();
		displayHoustonResults();

	//received the Austin search button event
	}else if (evt.getActionCommand().equals(ONE_CLICK_EVENT)){
	  austinSearchPanel = new OneClickStatusPanel();
	  this.remove(toNextCityPanel);
	  austinSearchPanel.setSize(255,430);
	  this.add(austinSearchPanel);
	  this.pack();
	  boolean hasMap = setAustinMap(austinMap);
	  austinSearchPanel.increaseProgressBar(10); //increase the progress bar size

	  //if successfully got the map, loads the Austin business XML file
	  if (hasMap) {
		  setSPA_Austin(austinXML);
		  austinSearchPanel.increaseProgressBar(10);
		  setTitle("CityFinder (current task: finds gas-stations near the UT in Austin, Texas)");
		  oneClickAutomation();
	  }
	}
  }
  
  /** Initializes the starting panel and its properties for the
   *  Houston search.
   */
  private void initializeHoustonSearch(){
	  startPanel = new StartPanel(this);
	  startPanel.setSize(710,465);
	  add(startPanel);
	  this.pack();
	  setTitle("CityFinder (current task: finds gas-stations near Galleria Mall in Houston, Texas U.S.A)");

	  //check the URL connection availability status by validating the destination image.
	  url_status = this.isUrlAvailable(this.urlStr + this.houstonMap);
	  //get Houston map
	  boolean hasMap = setHoustonMap(houstonMap);
	  //if successfully got the map, loads the Houston business XML file source
	  if (hasMap) setSPA_Houston(houstonXML);
  }

  /** Gets the Houston city map, and puts it on a Canvas to display. */
  private boolean setHoustonMap(String mapName){
	  Image mapImage = null;
	  ImageIcon imageIcon;
	  boolean hasFile = false;

	  //for displaying message on the panel.
	  startPanel.appendText(">> Connecting... \n" +
							">> Destination: " + urlStr + " \n");

	  if (url_status){//If URL Internet connection is available, downloads the image.
			try {
					URL url = new URL(urlStr + mapName); //get the Houston map from a remote Server
					imageIcon = new ImageIcon(url);
					mapImage = imageIcon.getImage();
					startPanel.appendText(">> Received Houston city Map... \n");
					hasFile = true;
			}catch(MalformedURLException em) {
					try{
							// If the network connection is not available, get the local image file.
							startPanel.appendText(">> Network resources are unavailable...  \n" +
												  ">> Use the local file instead \n");
							mapImage = getLocalImage(mapName, startPanel);
							hasFile = true;
					}catch(Exception ex){
						printError(startPanel, ex, mapName);
						return false;
					}
			}catch(Exception e){
				printError(startPanel, e, mapName);
			}
	  }else{//If URL connection is not available, use the local image file.
					try{
							// Get the local image instead.
							startPanel.appendText(">> Network resource file are not available...  \n" +
											      ">> Use the local files instead \n");
							mapImage = getLocalImage(mapName, startPanel);
							hasFile = true;
					}catch(Exception exc){
						printError(startPanel, exc, mapName);
					}

	  }

	  if (hasFile){
		  mapCanvas = new MapCanvas(mapImage);
		  return true;
	  }
	  return false;
  }

  /** Gets the Austin city map, and displays it on the Canvas. */
  private boolean setAustinMap(String mapName){
	  Image mapImage = null;
	  ImageIcon imageIcon;
	  boolean hasFile = false;


	  austinSearchPanel.appendText(">> Connecting... \n" +
							  ">> Destination: " + urlStr + " \n");

	  if (url_status){//If URL connection is available, downloads the image.
			try {
					URL url = new URL(urlStr + mapName); //get the Houston map from the Server
					imageIcon = new ImageIcon(url);
					mapImage = imageIcon.getImage();
					austinSearchPanel.appendText(">> Received the city Map from the remote URL server... \n");
					hasFile = true;
			}catch(MalformedURLException em) {
					try{
							//If URL connection is unavailable, get the local image instead.
							austinSearchPanel.appendText(">> Network resource file are unavailable...  \n" +
												 ">> Use the local file instead \n");
							mapImage = getLocalImage(mapName, austinSearchPanel);
							hasFile = true;
					}catch(Exception ex){
							printError(austinSearchPanel, ex, mapName);
							return false;
					}
			}catch(Exception e){
				printError(austinSearchPanel, e, mapName);
			}
	  }else{//If URL connection is unavailable, use the local image file.
					try{
							// Get the local image instead.
							mapImage = getLocalImage(mapName, austinSearchPanel);
							austinSearchPanel.appendText(">> Network resource file are unavailable...  \n" +
											   ">> Use the local file instead \n");
							mapImage = getLocalImage(mapName, austinSearchPanel);
							hasFile = true;
					}catch(Exception exc){
						printError(austinSearchPanel, exc, mapName);
					}

	  }

	  if (hasFile){
		  mapCanvas.reset(mapImage);  //resets the canvas with the new map
		  return true;
	  }
	  return false;
  }
  
  /** Displays the error on the screen. */
  private void printError(I_MsgDisplay msgScreen, Exception e, String fileLocation){
	msgScreen.printError(">> Exception: " + e + "\n" +
				         ">> Fatal error occurred : Can not access the resource files  \n" +
					     ">> File location: " + fileLocation +  "\n" +
					     ">> Please report the error \n");				  
  }
  
  /** Returns the Local image. */
  public Image getLocalImage(String imgName, I_MsgDisplay msgScreen) throws Exception{  
	ImageIcon imageIcon;
	
			msgScreen.appendText(">>Image Source: imgName = "+ imgName + "\n");
			imageIcon = new ImageIcon(imgName);
			
	return imageIcon.getImage();  
  }
  
  /** Simple way to validate a URL connection availability by checking the remote image.  */
  private boolean isUrlAvailable(String imageURL){
		   try{
			   URL url = new URL(imageURL);
			   return url.getContent() instanceof URLImageSource;
		   }catch(Exception e){}
		 return false;
  }

  /** Adds a store location on the Map as a red dot.
   *  @param x x-position
   *  @param y y-position
   *  @param rank rank of the store based on the user's preference
   *  @param id store id */
  protected void addStoreToMap(int x, int y, int rank, int id){
	  mapCanvas.addStore(x,y,rank,id);
  }

  /** Displays the Houston map and questions for the Houston search. */
  private void displayHoustonSearch(){
	  remove(startPanel);
	  mapCanvas.setSize(450,429);
	  add(mapCanvas);
	  displayQuestion();
  }

  /** Displays the question panel on the right side of the window frame.
   *  To collect a user's answers. */
  private void displayQuestion(){
	  questionPanel = new QuestionPanel(this,
								   ((GasDecisionProcess)decisionProcess).getNextPanelQuestion(),
								   QUE_NEXT_EVENT);
	  questionPanel.setSize(242,430);
	  this.add(questionPanel);
	  this.pack();
  }

  /** Displays the Houston city search results.  It displays the matched
   *  gas stations on the Houston map as red dots, the rank number next to the each dot,
   *  and displays the store's contact information on the TextArea of the right panel.
   **/
  protected void displayHoustonResults(){
	  ShopElement[] finalShopListInfo= decisionProcess.getShopListInfo();
	  String storeInfo = "Total number of Stores: " + finalShopListInfo.length + "\n";
	  
	  for (int i=0;i<finalShopListInfo.length;i++){
		  storeInfo += "\n " + (i+1) + "). Store: \n=============================\n" +
						finalShopListInfo[i].toString() + "\n";
			 
		  addStoreToMap(Integer.parseInt(finalShopListInfo[i].x),
						Integer.parseInt(finalShopListInfo[i].y),
						i+1,
						Integer.parseInt(finalShopListInfo[i].id));
	  }

	  mapCanvas.repaint();
	  toNextCityPanel = new ToNextCityPanel(this);
	  toNextCityPanel.setText(storeInfo);
	  this.remove(questionPanel);
	  toNextCityPanel.setSize(255,430);
	  this.add(toNextCityPanel);
	  this.pack();

  }

  /** Displays the Austin city search results.  **/
  protected void displayAustinResults(){

	  ShopElement[] bizInfoList= decisionProcess.getShopListInfo();
	  String storeInfo = "Total number of Stores: " + bizInfoList.length + "\n";

	  for (int i=0;i<bizInfoList.length;i++){
		  storeInfo += "\n " + (i+1) + "). Store: \n=============================\n" +
						bizInfoList[i].toString() + "\n";
		  addStoreToMap(Integer.parseInt(bizInfoList[i].x),
						Integer.parseInt(bizInfoList[i].y),
						i+1,
						Integer.parseInt(bizInfoList[i].id));
	  }

	  mapCanvas.repaint();
	  austinSearchPanel.appendText("\n\n================================\n" +
							  "Austin one-click Seach Result\n");
	  austinSearchPanel.appendText(storeInfo);
	  austinSearchPanel.displayFinalText();
  }

  /** Returns a string description */
  public String toString(){
	return "CityFinder (mobile navigation search engine) demo application. \n SPA: \n " +
		   decisionProcess.toString();
  }

  /** Start this CityFinder Demo application.  This application can be run as a regular application
   *  on a desktop computer or JavaWebStart application on web.  */
  public static void main(String args[]){
	CityFinderJWS cityFinder = new CityFinderJWS("http://chon.techliminal.com/cityfind/",
												   "houstonMap.gif",
												   "houstonXML.txt",
												   "austinMap.gif",
												   "austinXML.txt");
		
	cityFinder.setSize(740,510);
	cityFinder.show();
  }
}
