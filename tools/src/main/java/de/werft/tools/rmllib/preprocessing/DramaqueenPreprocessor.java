package de.werft.tools.rmllib.preprocessing;

import de.werft.tools.general.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This preprocessor takes a dramaqueen file and unzips the xml
 * file. In a second step the xml file is adjusted to match
 * the capabilities of rml.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class DramaqueenPreprocessor extends BasicPreprocessor {


    @Override
    protected URL preprocessInput(Document doc) {
        try {
            /* decompress dq files, which are just ordinary zip files */
            ZipFile zip = new ZipFile(doc.getInputFile().getFile());
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry ze = entries.nextElement();
                if ("document.xml".equals(ze.getName())) {
                    Path tmp = Files.createTempFile("dq", ".xml");
                    replacePropertyAttributes(zip.getInputStream(ze), tmp);
                    return tmp.toUri().toURL();
                }
            }

        } catch (IOException e) {
            logger.error("Could not unzip dramaqueen file. " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /* replace property ids with real string values */
    private void replacePropertyAttributes(InputStream io, Path outputFile) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            /* get all property nodes */
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(io);
            NodeList properties = doc.getElementsByTagName("Property");

            for (int i = 0; i < properties.getLength(); i++) {
                NamedNodeMap attributes = properties.item(i).getAttributes();
                /* replace integer value with string representation */
                Integer idValue = Integer.valueOf(attributes.getNamedItem("id").getTextContent());
                String replacement = idMappings.get(idValue);
                attributes.getNamedItem("id").setTextContent(replacement);

                /* handle gender attribute */
                if (attributes.getNamedItem("id").getTextContent().equals("character_gender")
                        && attributes.getNamedItem("value") != null) {
                    Node value = attributes.getNamedItem("value");
                    value.setTextContent(genderMapping.get(Integer.valueOf(value.getTextContent())));

                } else if (attributes.getNamedItem("value") != null) {
                    /* set the value attribute as node content */
                    String value = attributes.getNamedItem("value").getTextContent();
                    properties.item(i).setTextContent(value);
                }
            }

            /* store the changed xml in a temporary file */
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(Files.newOutputStream(outputFile, StandardOpenOption.TRUNCATE_EXISTING));
            transformer.transform(source, result);

        } catch (ParserConfigurationException | SAXException | TransformerException | IOException e) {
            logger.error("Could not preprocess dramaqueen xml.");
        }
    }

    /* mappings for gender informations */
    private final static HashMap<Integer, String> genderMapping = new HashMap<Integer, String>() {{
        put(0, "male");
        put(1, "female");
    }};

    /* too many mappings for dramaqueen properties */
    private final static HashMap<Integer, String> idMappings = new HashMap<Integer, String>() {{
        /* General properties */
        put(0, "name");
        put(1, "text");
        /* notes */
        put(8, "note");
        put(9, "outline");
        put(178, "created_by_user");
        put(252, "description");
        /* script document properties */
        put(2, "authors");
        put(127, "title_page"); /* deprecated */
        put(210, "title_page_expose");
        put(211, "title_page_treatment");
        put(212, "title_page_script");
        put(179, "annotation_authors");
        put(180, "manually_deleted_characters");
        put(188, "document_language");
        put(203, "ignore_words");
        put(273, "numbers_start_steps");
        put(274, "numbers_start_scenes");
        put(275, "locked_numbers_steps");
        put(276, "locked_numbers_scenes");
        put(280, "dialog_page_breaks");
        /* Main plot only properties (stored in ScriptDocument) */
        put(204, "theme");
        put(205, "central_question");
        put(196, "layout_new_page_before_steps");
        put(197, "layout_new_page_before_scenes");
        put(189, "layout_new_page_before_frames");
        put(198, "layout_new_page_before_sub_frames");
        put(190, "layout_font_normal");
        put(191, "layout_font_treatment");
        put(192, "layout_font_script");
        put(193, "layout_font_size_normal");
        put(194, "layout_font_size_treatment");
        put(195, "layout_font_size_script");
        put(213, "layout_template_normal");
        put(214, "layout_expose_step_spacing");
        put(215, "layout_treatment_step_spacing");
        put(216, "layout_treatment_scene_spacing");
        put(217, "layout_show_scene_headings");
        put(200, "layout_font_name");
        put(201, "layout_font_size");
        put(202, "layout_new_page_before_elements");
        put(148, "layout_show_numbers");
        put(133, "show_column_label");
        put(134, "show_column_enumeration");
        put(135, "show_column_plot");
        put(136, "show_column_mood");
        put(137, "show_column_function");
        put(138, "show_column_edit_state");
        put(139, "show_column_life_cycle");
        put(140, "show_column_location_int_ext");
        put(141, "show_column_location_time");
        put(142, "show_column_locations");
        put(143, "show_column_characters");
        put(144, "show_column_outline");
        put(185, "column_characters_sorting");
        put(145, "show_title_page_in_expose");
        put(146, "show_title_page_in_treatment");
        put(147, "show_title_page_in_script");
        put(148, "show_numbers_in_expose");
        put(149, "show_numbers_in_treatment");
        put(150, "show_numbers_in_script");
        put(166, "partial_text_beginning");
        put(172, "level_steps_activated");
        put(173, "level_scenes_activated");
        put(174, "level_frames_activated");
        /* Plot properties */
        put(16, "structure_model");
        put(128, "show_structure_model");
        put(130, "synchronize_element_order");
        put(131, "turnung_points_visible_in_story");
        put(132, "turning_points_visible");
        put(186, "automatic_plot_detection");
        put(187, "automatic_plot_detection_mode");
        put(157, "archive");
        put(20, "main_character_sex");
        put(21, "main_character_type");
        put(22, "main_character_anti_hero_type");
        put(153, "want_and_need");
        put(175, "follows_want_or_need");
        put(208, "new_want");
        put(23, "main_character_learns");
        put(152, "happy_end_type");
        put(158, "old_live_quality");
        put(207, "old_live_description");
        put(24, "impulse_direction");
        put(25, "plot_point_one_direction");
        put(83, "plot_point_two_direction");
        put(26, "mid_point_direction");
        put(27, "klimax_direction");
        put(277, "new_live_description");
        put(170, "guidance_part_main_plot");
        put(171, "guidance_part_sub_plot");
        put(77, "guidance_part_introduction");
        put(78, "guidance_part_structure_models");
        put(119, "guidance_part_model_three_acts");
        put(120, "guidance_part_model_turning_points");
        put(121, "guidance_part_model_eight_chapters");
        put(122, "guidance_part_model_twelve_stations");
        put(123, "guidance_part_model_open");
        put(169, "guidance_part_turning_points");
        put(79, "guidance_part_first_act_intro");
        put(80, "guidance_part_second_act_intro");
        put(81, "guidance_part_third_act_intro");
        put(28, "guidance_part_beginning");
        put(29, "guidance_part_episodes");
        put(73, "guidance_part_prolog");
        put(30, "guidance_part_main_characters");
        put(31, "guidance_part_properties");
        put(32, "guidance_part_dispositions");
        put(85, "guidance_part_linking_audience");
        put(33, "guidance_part_first_crisis");
        put(34, "guidance_part_movie_mood");
        put(35, "guidance_part_old_world");
        put(36, "guidance_part_hook");
        put(37, "guidance_part_kick_off");
        put(38, "guidance_part_refusal");
        put(39, "guidance_part_need");
        put(40, "guidance_part_dramatic_event");
        put(93, "guidance_part_dramatic_event_check");
        put(94, "guidance_part_turning_point_one");
        put(41, "guidance_part_direction_warning");
        put(42, "guidance_part_dramatic_fall");
        put(43, "guidance_part_dramatic_fall_check");
        put(159, "guidance_part_discrepancy_check");
        put(44, "guidance_part_problem");
        put(45, "guidance_part_motivation");
        put(46, "guidance_part_want");
        put(47, "guidance_part_want_and_need");
        put(154, "guidance_part_want_need_match");
        put(155, "guidance_part_want_need_conflict");
        put(176, "guidance_part_follows_need");
        put(177, "guidance_part_follows_need_capable");
        put(48, "guidance_part_conflict");
        put(49, "guidance_part_central_question");
        put(50, "guidance_part_more_questions");
        put(74, "guidance_part_goal");
        put(51, "guidance_part_point_of_no_return");
        put(52, "guidance_part_anticipation");
        put(76, "guidance_part_first_tries");
        put(90, "guidance_part_subplot");
        put(53, "guidance_part_antagonist");
        put(54, "guidance_part_collision");
        put(88, "guidance_part_bonfire");
        put(55, "guidance_part_dynamic_evolves");
        put(95, "guidance_part_midpoint");
        put(82, "guidance_part_after_midpoint");
        put(89, "guidance_part_positive_cont_warning");
        put(151, "guidance_part_continue_to_success");
        put(56, "guidance_part_holding_still");
        put(57, "guidance_part_hardest_test");
        put(91, "guidance_part_plots");
        put(75, "guidance_part_turning_point_2_check");
        put(84, "guidance_part_peripetie");
        put(58, "guidance_part_back_to_old_world");
        put(59, "guidance_part_final_test");
        put(60, "guidance_part_showdown");
        put(61, "guidance_part_meeting_scene");
        put(62, "guidance_part_final_test_encounter");
        put(86, "guidance_part_klimax_info");
        put(63, "guidance_part_revelation");
        put(64, "guidance_part_ability_to_learn");
        put(65, "guidance_part_inability_to_learn");
        put(87, "guidance_part_hero_usually_learns");
        put(66, "guidance_part_katharsis");
        put(156, "guidance_part_happy_end");
        put(67, "guidance_part_finalization");
        put(92, "guidance_part_subplots");
        put(68, "guidance_part_change");
        put(69, "guidance_part_new_balance");
        put(70, "guidance_part_finish");
        put(71, "guidance_part_open_end");
        put(72, "guidance_part_kiss_off");
        put(281, "pre_text_expose");
        put(282, "pre_text_treatment");
        put(283, "pre_text_script");
        put(284, "show_pre_text_expose");
        put(285, "show_pre_text__treatment");
        put(286, "show_pre_text_script");
        /* Properties of Plot objects */
        put(96, "plot_color");
        put(118, "main_character");
        /* Properties of TurningPoint objects */
        put(206, "turning_point_description");
        /* Properties of Station objects */
        put(12, "plot_point_outline");
        put(13, "three_acts_outline");
        put(279, "five_acts_outline");
        put(14, "turning_point_outline");
        put(15, "eight_sequences_outline");
        /* Scenes and Steps */
        put(11, "plot");
        put(7, "mood");
        put(19, "development");
        put(17, "edit_state");
        put(97, "live_cycle");
        put(115, "dramatic_function");
        /* Scenes and Frames */
        put(167, "was_first_element");
        /* Properties of Scenes */
        put(18, "character_list");
        /* Frame properties */
        put(10, "location");
        put(3, "inside");
        put(4, "outside");
        put(168, "inside_outside");
        put(5, "time_of_day");
        put(6, "stop_duration");
        put(126, "dynamic_time_of_day");
        put(278, "scene_heading");
        /* Character properties */
        put(181, "character_full_name");
        put(182, "character_full_name_internal");
        put(209, "character_alias_names");
        put(98, "character_age");
        put(183, "character_age_internal");
        put(117, "character_gender");
        put(184, "character_gender_internal");
        put(99, "character_look");
        put(100, "character_relationship_status");
        put(101, "character_social_status");
        put(102, "character_occupation");
        put(103, "character_hobbies");
        put(104, "character_biography_stations");
        put(105, "character_credo");
        put(160, "character_strength");
        put(106, "character_weakness");
        put(107, "character_trauma");
        put(161, "character_fear");
        put(108, "character_peculiarities");
        put(162, "character_selfperception");
        put(163, "character_externalperception");
        put(164, "character_backstory");
        put(109, "character_dramaturgic_function");
        put(110, "character_want");
        put(111, "character_motivation");
        put(113, "character_need");
        put(112, "character_conflict");
        put(165, "character_fall");
        put(114, "character_development");
        /* Location properties */
        put(124, "location_dramaturgic_function");
        put(125, "location_development");
        /* TimeOfDay properties */
        put(199, "time_of_day_which");
        /* StationProxy */
        put(129, "station_count");
        /* ParagraphSettings */
        put(264, "paragraph_type");
        put(218, "paragraph_alignment");
        put(219, "paragraph_spacing_top");
        put(220, "paragraph_spacing_bottom");
        put(243, "paragraph_has_numbering");
        put(221, "paragraph_first_line_indent");
        put(222, "paragraph_line_indent");
        put(223, "paragraph_rught_indent");
        put(224, "paragraph_line_spacing");
        put(265, "paragraph_glyph_spacing");
        put(225, "paragraph_flags_all_caps");
        put(226, "paragraph_page_break_before");
        put(227, "paragraph_page_break_after");
        put(239, "paragraph_no_page_break_before");
        put(242, "paragraph_no_page_break_after");
        put(228, "paragraph_lines_before_page_break");
        put(229, "paragraph_lines_after_page_break");
        put(244, "paragraph_overridden_font_name");
        put(245, "paragraph_font_size");
        put(246, "paragraph_font_bold");
        put(247, "paragraph_font_italic");
        put(248, "paragraph_font_underline");
        put(249, "paragraph_font_strikeout");
        put(250, "paragraph_font_fg_color");
        put(251, "paragraph_font_bg_color");
        /* TextSettingsTemplate */
        put(230, "page_width");
        put(231, "page_height");
        put(232, "page_margin_left");
        put(233, "page_margin_right");
        put(234, "page_margin_top");
        put(235, "page_margin_bottom");
        put(253, "page_header_footer_config_separate");
        put(254, "page_number_start");
        put(266, "page_header_footer_start");
        put(240, "layout_numbering_pos_left");
        put(241, "layout_numbering_pos_right");
        put(259, "layout_numbering_placement");
        put(258, "layout_scene_heading_layout");
        put(260, "layout_step_spacing_mode");
        put(261, "layout_step_spacing");
        put(262, "layout_scene_spacing_mode");
        put(263, "layout_scene_spacing");
        /* HeaderFooterSettings */
        put(255, "page_header_footer_show");
        put(256, "page_header_footer_mode");
        put(257, "page_header_footer_number_alignment");
        put(236, "page_header_footer_spacing");
        /* TextSettings */
        put(238, "layout_template_index");
        put(267, "numbering_font_bold");
        put(268, "numbering_font_italic");
        put(269, "numbering_font_underline");
        put(270, "numbering_font_strikeout");
        put(271, "numbering_font_fg_color");
        put(272, "numbering_font_bg_color");
    }};

}
