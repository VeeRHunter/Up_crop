
#import "PhotoCropViewController.h"
#import "CVWrapper.h"

@interface PhotoCropViewController ()

@end

CGFloat screenHeight;
CGFloat screenWid;

@implementation PhotoCropViewController{
    RegionSelect* rs;
}


- (void)viewDidLoad {
    [super viewDidLoad];
    
    CGRect screenRect = [[UIScreen mainScreen] bounds];
    screenHeight = screenRect.size.height;
    screenWid = screenRect.size.width;
    
    
    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
    [button addTarget:self
               action:@selector(newPhotoClick:)
     forControlEvents:UIControlEventTouchUpInside];
    [button setTitle:@"New Photo" forState:UIControlStateNormal];
    button.frame = CGRectMake(screenWid/4-50, screenHeight - 40, 100, 30);
    button.backgroundColor = [UIColor redColor];
    [self.view addSubview:button];
    
    UIButton *button1 = [UIButton buttonWithType:UIButtonTypeCustom];
    [button1 addTarget:self
               action:@selector(cropClick:)
     forControlEvents:UIControlEventTouchUpInside];
    button1.backgroundColor = [UIColor redColor];
    [button1 setTitle:@"Crop" forState:UIControlStateNormal];
    button1.frame = CGRectMake(screenWid/4*3-50, screenHeight - 40, 100, 30);
    [self.view addSubview:button1];
}

-(IBAction)newPhotoClick:(id)sender{
    
    UIImagePickerController *picker = [[UIImagePickerController alloc] init];
//    picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    picker.delegate = self;
//    [self presentViewController:picker animated:YES completion:nil];
    picker.allowsEditing = YES;
    picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    [self presentViewController:picker animated:YES completion:nil];
    
//    [self dismissViewControllerAnimated:NO completion:nil];
//    ViewController *controler = [[ViewController alloc] initWithNibName:nil bundle:nil];
//    [self presentViewController:controler animated:YES completion:NULL];
}

#pragma mark -
#pragma mark UIImagePickerControllerDelegate

- (void) imagePickerController:(UIImagePickerController *)picker
         didFinishPickingImage:(UIImage *)image
                   editingInfo:(NSDictionary *)editingInfo
{
    //self.imageView.image = image;
    [self dismissModalViewControllerAnimated:YES];
    _img = image;
    
    rs = [[RegionSelect alloc] initWithFrame:CGRectMake(0, (screenHeight - screenWid/9*16)/2, screenWid, screenWid/9*16 - 50)];
    
    rs.ltx = rs.bounds.size.width / 4;
    rs.lty = rs.bounds.size.height / 4;
    rs.rtx = rs.bounds.size.width / 4;
    rs.rty = rs.bounds.size.height * 3 / 4;
    rs.rbx = rs.bounds.size.width * 3 / 4;
    rs.rby = rs.bounds.size.height * 3 / 4;
    rs.lbx = rs.bounds.size.width * 3 / 4;
    rs.lby = rs.bounds.size.height / 4;
    
    NSLog(@"%@", [NSString stringWithFormat:@"%f",_x1]);
    
    UIGraphicsBeginImageContextWithOptions(rs.bounds.size, NO, 0.0);
    [_img drawInRect:rs.bounds];
    UIImage *image_Old = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    rs.backgroundColor = [UIColor colorWithPatternImage:image_Old];
    [[self view] addSubview:rs];
    
}


-(IBAction)cropClick:(id)sender{
    ResultController *controler = [[ResultController alloc] initWithNibName:nil bundle:nil];
    

    CGPoint firstPt = [self makePoints:rs.lbx fy:rs.lby];
    CGPoint secondPt = [self makePoints:rs.rbx fy:rs.rby];
    CGPoint thirdPt = [self makePoints:rs.rtx fy:rs.rty];
    CGPoint fourthPt = [self makePoints:rs.ltx fy:rs.lty];
    NSLog(@"%@", [NSString stringWithFormat:@"%f",rs.lbx]);
    controler.resultimg = [CVWrapper cropImage:_img firstPt:firstPt secondPt:secondPt thirdPt:thirdPt fourthPt:fourthPt];
    [self presentViewController:controler animated:YES completion:nil];
}

- (CGPoint) makePoints:(CGFloat) rx1 fy:(CGFloat) ry1
{
    
    CGFloat layerwid = screenWid;
    CGFloat layerhei = screenWid/9*16;
    
    CGFloat imgwid = _img.size.width;
    CGFloat imghei = _img.size.height;
    
    CGFloat scalewid = layerwid/imgwid;
    CGFloat scalehei = layerhei/imghei;
    
    return (CGPointMake(rx1/scalewid, (ry1)/scalehei));
}

-(UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

-(NSUInteger)navigationControllerSupportedInterfaceOrientations:(UINavigationController *)navigationController {
    return UIInterfaceOrientationMaskPortrait;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



@end
